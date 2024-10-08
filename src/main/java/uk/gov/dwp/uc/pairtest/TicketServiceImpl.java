package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.PurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.function.Function;

/**
 * - There are 3 types of tickets i.e. Infant, Child, and Adult.
 * - The ticket prices are based on the type of ticket (see table below).
 * - The ticket purchaser declares how many and what type of tickets they want to buy.
 * - Multiple tickets can be purchased at any given time.
 * - Only a maximum of 25 tickets that can be purchased at a time.
 * - Infants do not pay for a ticket and are not allocated a seat. They will be sitting on an Adult's lap.
 * - Child and Infant tickets cannot be purchased without purchasing an Adult ticket.
 */
public class TicketServiceImpl implements TicketService {

    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;
    private static final int INFANT_TICKET_PRICE = 0;

    private static final String EMPTY_REQUEST_WARNING = "Ticket requests cannot be null";

    private final SeatReservationService seatReservationService;
    private final TicketPaymentService ticketPaymentService;

    // Added this constructor for the unit test
    public TicketServiceImpl(SeatReservationService seatReservationService, TicketPaymentService ticketPaymentService) {
        this.seatReservationService = seatReservationService;
        this.ticketPaymentService = ticketPaymentService;
    }

    /**
     * Should only have private methods other than the one below.
     */

    /**
     * Purchases tickets for the given account ID and ticket type requests.
     * Validates the account ID and ticket type requests, creates a purchase request,
     * reserves seats, and makes the payment.
     *
     * @param accountId          the account ID
     * @param ticketTypeRequests the ticket type requests
     * @throws InvalidPurchaseException if the purchase request is invalid
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccountId(accountId);

        if (ticketTypeRequests == null) {
            throw new InvalidPurchaseException(EMPTY_REQUEST_WARNING);
        }

        PurchaseRequest purchaseRequest = createPurchaseRequest(ticketTypeRequests);

        seatReservationService.reserveSeat(accountId, purchaseRequest.getNoOfTickets());
        ticketPaymentService.makePayment(accountId, purchaseRequest.getTotalAmount());
    }

    /**
     * Creates a purchase request from the given ticket type requests.
     * Validates the ticket type requests, calculates the total number of tickets and total amount to pay,
     * and ensures that there is at least one adult and the ticket count is reasonable.
     *
     * @param ticketTypeRequests the ticket type requests
     * @return the created PurchaseRequest
     */
    private PurchaseRequest createPurchaseRequest(TicketTypeRequest... ticketTypeRequests) {
        boolean hasAdult = false;
        int totalNoOfTicketsToPurchase = 0;
        int totalAmountToPay = 0;
        int totalNoOfTicketsWithInfant = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            if (ticketTypeRequest == null) {
                throw new InvalidPurchaseException(EMPTY_REQUEST_WARNING);
            }

            if (ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.ADULT) {
                hasAdult = true;
            }

            totalNoOfTicketsWithInfant += ticketTypeRequest.getNoOfTickets();

            if (ticketTypeRequest.getTicketType() != TicketTypeRequest.Type.INFANT) {
                totalNoOfTicketsToPurchase += ticketTypeRequest.getNoOfTickets();
                totalAmountToPay += GET_TICKET_PRICE.apply(ticketTypeRequest) * ticketTypeRequest.getNoOfTickets();
            }
        }

        // Validation is done here to optimise the code,
        // so that we are not making multiple iterations over the same data.
        acceptOnlyIfAdultIsPresent(hasAdult);

        acceptOnlyIfTicketCountIsResonable(totalNoOfTicketsWithInfant);

        return new PurchaseRequest(totalNoOfTicketsToPurchase, totalAmountToPay);
    }


    /**
     * Validates if the account ID is valid.
     * Throws InvalidPurchaseException if the account ID is null or less than 1.
     *
     * @param accountId the account ID to validate
     */
    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId < 1) {
            throw new InvalidPurchaseException("Please provide a valid account id");
        }
    }

    /**
     * Ensures that there is at least one adult in the ticket type requests.
     * Throws InvalidPurchaseException if no adult is present.
     *
     * @param hasAdult boolean indicating if there is at least one adult
     */
    private void acceptOnlyIfAdultIsPresent(boolean hasAdult) {
        if (!hasAdult) {
            throw new InvalidPurchaseException("There should be at-least one Adult");
        }
    }

    /**
     * Validates if the total number of tickets to purchase is reasonable.
     * Throws InvalidPurchaseException if the number of tickets is not between 1 and 25.
     *
     * @param totalNoOfTicketsToPurchase the total number of tickets to purchase
     */
    private void acceptOnlyIfTicketCountIsResonable(int totalNoOfTicketsToPurchase) {
        if (totalNoOfTicketsToPurchase > 25 || totalNoOfTicketsToPurchase < 1) {
            throw new InvalidPurchaseException("Number of tickets must be between 1 and 25 for a single transaction");
        }
    }

    /**
     * Get the price of a ticket based on its type.
     * Returns the price for ADULT, CHILD, and INFANT ticket types.
     */
    private static final Function<TicketTypeRequest, Integer> GET_TICKET_PRICE = ticketTypeRequest -> {
        switch (ticketTypeRequest.getTicketType()) {
            case ADULT:
                return ADULT_TICKET_PRICE;
            case CHILD:
                return CHILD_TICKET_PRICE;
            case INFANT:
            default:
                return INFANT_TICKET_PRICE;
        }
    };
}
