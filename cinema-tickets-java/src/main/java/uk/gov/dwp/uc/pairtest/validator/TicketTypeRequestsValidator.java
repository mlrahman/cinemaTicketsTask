package uk.gov.dwp.uc.pairtest.validator;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketTypeRequestsValidator {

    public void validateTicketTypeRequests(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("No tickets requested");
        }

        int totalTickets = 0;
        int adultTickets = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException(
                    "Invalid number of tickets: " + request.getNoOfTickets() + " for type: " + request.getTicketType());
            }

            if (request.getTicketType() == ADULT) {
                adultTickets += request.getNoOfTickets();
            }

            if (request.getTicketType() != INFANT) {
                totalTickets += request.getNoOfTickets();
            }
        }

        if (totalTickets > 25) {
            throw new InvalidPurchaseException("Cannot purchase more than 25 tickets at a time");
        }

        if (adultTickets == 0) {
            throw new InvalidPurchaseException("Child and Infant tickets cannot be purchased without an Adult ticket");
        }
    }

}
