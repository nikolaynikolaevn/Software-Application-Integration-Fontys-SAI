package client;

import client.model.TravelRefundReply;
import client.model.TravelRefundRequest;

public interface TravelRefundReplyListener {
    public void onReplyReceived(TravelRefundRequest request, TravelRefundReply reply);
}
