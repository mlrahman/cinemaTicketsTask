package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.CHILD;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.validator.TicketTypeRequestsValidator;
import uk.gov.dwp.uc.pairtest.validator.UserAccountValidator;

class TicketServiceImplTest {
  @Mock
  private TicketPaymentService ticketPaymentService;

  @Mock
  private SeatReservationService seatReservationService;

  @Mock
  private UserAccountValidator userAccountValidator;

  @Mock
  private TicketTypeRequestsValidator ticketTypeRequestsValidator;

  @InjectMocks
  private TicketServiceImpl ticketService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void shouldFailWhenUserAccountIdInvalid() {
    long accountId = 0L;
    TicketTypeRequest[] requests = {
        new TicketTypeRequest(ADULT, 2)
    };

    doThrow(new InvalidPurchaseException("No tickets requested"))
        .when(userAccountValidator).validateAccountId(accountId);

    InvalidPurchaseException exception = assertThrows(
        InvalidPurchaseException.class,
        () -> ticketService.purchaseTickets(accountId, requests)
    );
    assertEquals("Invalid account ID: 0", exception.getMessage());

    verify(ticketPaymentService, never()).makePayment(anyLong(), anyInt());
    verify(seatReservationService, never()).reserveSeat(anyLong(), anyInt());
  }

  @Test
  void shouldFailWhenNoTicketsRequested() {
    long accountId = 1L;
    TicketTypeRequest[] requests = {};

    doThrow(new InvalidPurchaseException("No tickets requested"))
        .when(ticketTypeRequestsValidator).validateTicketTypeRequests(requests);

    InvalidPurchaseException exception = assertThrows(
        InvalidPurchaseException.class,
        () -> ticketService.purchaseTickets(accountId, requests)
    );
    assertEquals("No tickets requested", exception.getMessage());

    verify(ticketPaymentService, never()).makePayment(anyLong(), anyInt());
    verify(seatReservationService, never()).reserveSeat(anyLong(), anyInt());
  }

  @Test
  void shouldFailWhenInvalidNumberOfTickets() {
    long accountId = 1L;
    TicketTypeRequest[] requests = {
        new TicketTypeRequest(ADULT, -2)
    };

    doThrow(new InvalidPurchaseException("Invalid number of tickets: -2 for type: ADULT"))
        .when(ticketTypeRequestsValidator).validateTicketTypeRequests(requests);

    InvalidPurchaseException exception = assertThrows(
        InvalidPurchaseException.class,
        () -> ticketService.purchaseTickets(accountId, requests)
    );
    assertEquals("Invalid number of tickets: -2 for type: ADULT", exception.getMessage());

    verify(ticketPaymentService, never()).makePayment(anyLong(), anyInt());
    verify(seatReservationService, never()).reserveSeat(anyLong(), anyInt());
  }

  @Test
  void shouldFailWhenMoreThan25TicketsRequested() {
    long accountId = 1L;
    TicketTypeRequest[] requests = {
        new TicketTypeRequest(ADULT, 26)
    };

    doThrow(new InvalidPurchaseException("Cannot purchase more than 25 tickets at a time"))
        .when(ticketTypeRequestsValidator).validateTicketTypeRequests(requests);

    InvalidPurchaseException exception = assertThrows(
        InvalidPurchaseException.class,
        () -> ticketService.purchaseTickets(accountId, requests)
    );
    assertEquals("Cannot purchase more than 25 tickets at a time", exception.getMessage());

    verify(ticketPaymentService, never()).makePayment(anyLong(), anyInt());
    verify(seatReservationService, never()).reserveSeat(anyLong(), anyInt());
  }

  @Test
  void shouldFailWhenChildAndInfantTicketsWithoutAdult() {
    long accountId = 1L;
    TicketTypeRequest[] requests = {
        new TicketTypeRequest(CHILD, 1),
        new TicketTypeRequest(INFANT, 1)
    };

    doThrow(new InvalidPurchaseException("Child and Infant tickets cannot be purchased without an Adult ticket"))
        .when(ticketTypeRequestsValidator).validateTicketTypeRequests(requests);

    InvalidPurchaseException exception = assertThrows(
        InvalidPurchaseException.class,
        () -> ticketService.purchaseTickets(accountId, requests)
    );
    assertEquals("Child and Infant tickets cannot be purchased without an Adult ticket", exception.getMessage());

    verify(ticketPaymentService, never()).makePayment(anyLong(), anyInt());
    verify(seatReservationService, never()).reserveSeat(anyLong(), anyInt());
  }

  @Test
  void shouldPassValidationAndProcessPaymentAndReservation() {
    long accountId = 1L;
    TicketTypeRequest[] requests = {
        new TicketTypeRequest(ADULT, 2),
        new TicketTypeRequest(CHILD, 1),
        new TicketTypeRequest(INFANT, 1)
    };

    assertDoesNotThrow(() -> ticketService.purchaseTickets(accountId, requests));
  }

}
