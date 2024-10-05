package uk.gov.dwp.uc.test.pairtest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TicketServiceImplTest {

    @Mock
    private SeatReservationService seatReservationService;

    @Mock
    private TicketPaymentService payments;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketService = new TicketServiceImpl(seatReservationService, payments);
    }

    @Test
    public void testValidAdultTicketPurchase(){
        // Given
        TicketTypeRequest[] ticketTypeRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
        };

        // When
        ticketService.purchaseTickets(1L, ticketTypeRequests);

        // Then
        verify(seatReservationService, times(1)).reserveSeat(1L, 1);
        verify(payments, times(1)).makePayment(1L, 25);
    }

    @Test
    public void testValidAdultAndChildTicketPurchase(){
        // Given
        TicketTypeRequest[] ticketTypeRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)
        };
        // When
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        // Then
        verify(seatReservationService, times(1)).reserveSeat(1L, 2);
        verify(payments, times(1)).makePayment(1L, 35);
    }

    @Test
    public void testValidAllTicketPurchase(){
        // Given
        TicketTypeRequest[] ticketTypeRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };
        // When
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        // Then
        verify(seatReservationService, times(1)).reserveSeat(1L, 2);
        verify(payments, times(1)).makePayment(1L, 35);
    }

    @Test
    public void testValidMultipleAdultTicketPurchase(){
        // Given
        TicketTypeRequest[] ticketTypeRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5)
        };

        // When
        ticketService.purchaseTickets(1L, ticketTypeRequests);

        // Then
        verify(seatReservationService, times(1)).reserveSeat(1L, 5);
        verify(payments, times(1)).makePayment(1L, 125);
    }

    @Test
    public void testValidMultipleAdultAndChildTicketPurchase(){
        // Given
        TicketTypeRequest[] ticketTypeRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5)
        };
        // When
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        // Then
        verify(seatReservationService, times(1)).reserveSeat(1L, 10);
        verify(payments, times(1)).makePayment(1L, 175);
    }

    @Test
    public void testValidMultipleAllTicketPurchase(){
        // Given
        TicketTypeRequest[] ticketTypeRequests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5)
        };
        // When
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        // Then
        verify(seatReservationService, times(1)).reserveSeat(1L, 20);
        verify(payments, times(1)).makePayment(1L, 350);
    }
}