package client;

import client.model.TravelRefundReply;
import client.model.TravelRefundRequest;

public interface TravelRefundRequestListener {
    public void onRequestReceived(TravelRefundRequest request, TravelRefundReply reply);
}
