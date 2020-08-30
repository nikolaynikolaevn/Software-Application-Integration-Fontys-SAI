package approval;

import approval.model.ApprovalReply;
import approval.model.ApprovalRequest;
import com.google.gson.Gson;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

public class TravelBrokerAppGateway implements MessageListener {

    private MessageSenderGateway messageSenderGateway;
    private MessageReceiverGateway messageReceiverGateway;

    private ApprovalRequestListener ls;

    HashMap<ApprovalRequest, String> requestMap; // approval request to its JMS ID

    Gson gson;

    public TravelBrokerAppGateway(String approvalRequestQueue) {
        messageSenderGateway = new MessageSenderGateway("approvalReplyQueue");
        messageReceiverGateway = new MessageReceiverGateway(approvalRequestQueue);
        requestMap = new HashMap<>();

        gson = new Gson();

        messageReceiverGateway.setListener(this);
    }

    public void sendApproval(ApprovalRequest request, ApprovalReply reply) {
        Message msg = messageSenderGateway.createTextMessage(gson.toJson(reply));
        try {
            msg.setJMSCorrelationID(requestMap.get(request));
            messageSenderGateway.send(msg);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void setRequestListener(ApprovalRequestListener ls) {
        this.ls = ls;
    }

    @Override
    public void onMessage(Message message) {
        try {
            ApprovalRequest approvalRequest = gson.fromJson(((TextMessage) message).getText(), ApprovalRequest.class);
            requestMap.put(approvalRequest, message.getJMSMessageID());
            ls.onRequestReceived(approvalRequest, null);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
