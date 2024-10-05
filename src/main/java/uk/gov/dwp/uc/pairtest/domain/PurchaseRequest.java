package uk.gov.dwp.uc.pairtest.domain;

public class PurchaseRequest {
    private final int noOfTickets;
    private final int totalAmount;

    public PurchaseRequest(int noOfTickets, int totalAmount) {
        this.noOfTickets = noOfTickets;
        this.totalAmount = totalAmount;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public int getTotalAmount() {
        return totalAmount;
    }
}
