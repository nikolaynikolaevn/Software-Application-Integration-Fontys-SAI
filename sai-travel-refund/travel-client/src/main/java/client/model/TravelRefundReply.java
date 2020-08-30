package client.model;

/**
 *
 * This class stores all information about a travel approval reply as a response
 * to a travel refund request.
 */
public class TravelRefundReply {

    private boolean approved;
    private String reasonRejected;
    private double costs;

    public TravelRefundReply() {
    }

    public TravelRefundReply(boolean approved, String reasonRejected, double costs) {
        this.approved = approved;
        this.reasonRejected = reasonRejected;
        this.costs = costs;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getReasonRejected() {
        return reasonRejected;
    }

    public void setReasonRejected(String reasonRejected) {
        this.reasonRejected = reasonRejected;
    }
    
        public double getCosts() {
        return costs;
    }

    public void setCosts(double costs) {
        this.costs = costs;
    }

    @Override
    public String toString() {
        return (approved ? ("approved, costs="+costs) : ("rejected by " + reasonRejected));
    }


}
