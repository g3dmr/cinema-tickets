package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 10;
    private static final int INFANT_TICKET_PRICE = 0;

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

    private final SeatReservationService seatReservationService;
    private final TicketPaymentService ticketPaymentService;

    public TicketServiceImpl(SeatReservationService seatReservationService, TicketPaymentService ticketPaymentService) {
        this.seatReservationService = seatReservationService;
        this.ticketPaymentService = ticketPaymentService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (accountId == null || accountId < 1) {
            throw new InvalidPurchaseException("Please provide a valid account id");
        }
        if (ticketTypeRequests == null) {
            throw new InvalidPurchaseException("TicketTypeRequests cannot be null");
        }

        AtomicBoolean hasAdult = new AtomicBoolean(false);
        AtomicInteger totalNoOfTicketsInRequest = new AtomicInteger(0);
        AtomicInteger totalAmountToPay = new AtomicInteger(0);

        //Iterating over the ticketTypeRequests to calculate the total amount to pay and
        // total no of tickets in the request.
        // Also checking if there is at-least one Adult in the request.

        Arrays.stream(ticketTypeRequests).forEach(request -> {
            if (request == null) {
                throw new InvalidPurchaseException("TicketTypeRequests cannot be null");
            }

            if (request.getTicketType() == TicketTypeRequest.Type.ADULT) {
                hasAdult.set(true);
            }
            if (request.getTicketType() != TicketTypeRequest.Type.INFANT) {
                totalNoOfTicketsInRequest.addAndGet(request.getNoOfTickets());
                totalAmountToPay.addAndGet(GET_TICKET_PRICE.apply(request) * request.getNoOfTickets());
            }
        });

        // Validation is done here to optimise the code,
        // so that we are not making multiple iterations over the same data.

        if (!hasAdult.get()) {
            throw new InvalidPurchaseException("There should be at-least one Adult");
        }

        if (totalNoOfTicketsInRequest.get() > 25 || totalNoOfTicketsInRequest.get() < 1) {
            throw new InvalidPurchaseException("Number of tickets must be between 1 and 25 for a single transaction");
        }

        seatReservationService.reserveSeat(accountId, totalNoOfTicketsInRequest.get());
        ticketPaymentService.makePayment(accountId, totalAmountToPay.get());
    }

}
