package client;

import client.model.TravelRefundReply;
import client.model.TravelRefundRequest;
import com.google.gson.Gson;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

public class TravelBrokerAppGateway implements MessageListener {

    private MessageSenderGateway messageSenderGateway;
    private MessageReceiverGateway messageReceiverGateway;

    private TravelRefundReplyListener ls;

    HashMap<String, TravelRefundRequest> requestMap;

    Gson gson;

    public TravelBrokerAppGateway() {
        messageSenderGateway = new MessageSenderGateway("travelRefundRequestQueue");
        messageReceiverGateway = new MessageReceiverGateway(null);
        requestMap = new HashMap<>();

        gson = new Gson();

        messageReceiverGateway.setListener(this);
    }

    public void sendTravelRefundRequest(TravelRefundRequest request, TravelRefundReply reply) {
        Message msg = messageSenderGateway.createTextMessage(gson.toJson(request));
        try {
            msg.setJMSReplyTo(messageReceiverGateway.destination);
            messageSenderGateway.send(msg);
            requestMap.put(msg.getJMSMessageID(), request);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void setRequestListener(TravelRefundReplyListener ls) {
        this.ls = ls;
    }

    @Override
    public void onMessage(Message message) {
        try {
            TravelRefundRequest request = requestMap.get(message.getJMSCorrelationID());
            TravelRefundReply reply = gson.fromJson(((TextMessage) message).getText(), TravelRefundReply.class);
            ls.onReplyReceived(request, reply);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
