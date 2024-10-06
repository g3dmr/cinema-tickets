package uk.gov.dwp.uc.test.pairtest;

import org.junit.Test;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.hamcrest.MatcherAssert.assertThat;

public class TicketServiceImplWarningMessagesTest {

    private static final String NO_ADULT_WARNING = "There should be at-least one Adult";
    private static final String EMPTY_REQUEST_WARNING = "Ticket requests cannot be null";
    private static final String INVALID_ACCOUNT_ID_WARNING = "Please provide a valid account id";
    private static final String MAX_TICKET_WARNING = "Number of tickets must be between 1 and 25 for a single transaction";

    @Test
    public void testWarningForNoAdultTicketWithChild() {
        validateException(1L, NO_ADULT_WARNING, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1));
    }

    @Test
    public void testWarningForNoAdultTicketWithInfant() {
        validateException(1L, NO_ADULT_WARNING, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));
    }

    @Test
    public void testWarningForValidAccountId() {
        TicketTypeRequest[] ticketTypeRequests ={
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        };
        validateException(0L, INVALID_ACCOUNT_ID_WARNING, ticketTypeRequests);
    }

    @Test
    public void testWarningForEmptyAccountId() {
        TicketTypeRequest[] ticketTypeRequests ={
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        };
        validateException(null, INVALID_ACCOUNT_ID_WARNING, ticketTypeRequests);
    }

    @Test
    public void testWarningForMaxTicketPurchase() {
        TicketTypeRequest[] ticketTypeRequests ={
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)
        };
        validateException(1L, MAX_TICKET_WARNING, ticketTypeRequests);
    }

    @Test
    public void testWarningForMaxTicketMultipleCategories() {
        TicketTypeRequest[] ticketTypeRequests ={
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 6)

        };
        validateException(1L, MAX_TICKET_WARNING, ticketTypeRequests);
    }

    public void testWarningForMaxTicketWithInfantCategories() {
        TicketTypeRequest[] ticketTypeRequests ={
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 4)

        };
        validateException(1L, MAX_TICKET_WARNING, ticketTypeRequests);
    }

    @Test
    public void testWarningForEmptyTicketType() {
        validateException(1L, EMPTY_REQUEST_WARNING, null);
    }

    @Test
    public void testWarningForEmptyTicketInList() {
        TicketTypeRequest[] ticketTypeRequests ={
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 6),
                null
        };
        validateException(1L, EMPTY_REQUEST_WARNING, ticketTypeRequests);
    }

    /**
     * Adopted this method to validate the exception thrown for each type.
     **/
    private void validateException(Long accountId, String exceptionMessage, TicketTypeRequest... ticketTypeRequests){
        TicketService exceptionService = new TicketServiceImpl(new SeatReservationServiceImpl(), new TicketPaymentServiceImpl());
        try {
            exceptionService.purchaseTickets(accountId, ticketTypeRequests);
        } catch (InvalidPurchaseException invalidPurchaseException) {
            assertThat("Exception message is not as expected", invalidPurchaseException.getMessage().equals(exceptionMessage));
        }
    }
}