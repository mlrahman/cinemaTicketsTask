package uk.gov.dwp.uc.pairtest;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.validator.TicketTypeRequestsValidator;
import uk.gov.dwp.uc.pairtest.validator.UserAccountValidator;

public class TicketServiceImpl implements TicketService {

    private final UserAccountValidator userAccountValidator = new UserAccountValidator();
    private final TicketTypeRequestsValidator ticketTypeRequestsValidator = new TicketTypeRequestsValidator();
    private final SeatReservationServiceImpl seatReservationService = new SeatReservationServiceImpl();
    private final TicketPaymentServiceImpl ticketPaymentService = new TicketPaymentServiceImpl();

    @Override
    public void purchaseTickets(final Long accountId, final TicketTypeRequest... ticketTypeRequests)
        throws InvalidPurchaseException {
        userAccountValidator.validateAccountId(accountId);
        ticketTypeRequestsValidator.validateTicketTypeRequests(ticketTypeRequests);
        final var totalPrice = getTotalPrice(ticketTypeRequests);
        final var totalTickets = getTotalTickets(ticketTypeRequests);
        seatReservationService.reserveSeat(accountId, totalTickets);
        ticketPaymentService.makePayment(accountId, totalPrice);
    }

    private int getTotalPrice(final TicketTypeRequest... ticketTypeRequests) {
        int totalPrice = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            totalPrice += request.getPrice() * request.getNoOfTickets();
        }
        return totalPrice;
    }

    private int getTotalTickets(final TicketTypeRequest... ticketTypeRequests) {
        int totalTickets = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request.getTicketType() != INFANT) {
                totalTickets += request.getNoOfTickets();
            }
        }
        return totalTickets;
    }

}
