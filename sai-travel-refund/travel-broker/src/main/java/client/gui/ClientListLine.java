package client.gui;

import client.model.ApprovalReply;
import client.model.TravelRefundReply;
import client.model.TravelRefundRequest;
import com.google.gson.Gson;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Objects;

public class ClientListLine<REQUEST, REPLY> implements MessageHolder<REQUEST, REPLY> {

    private Gson gson = new Gson();

    private final REQUEST request;
    private REPLY reply;

    private REQUEST firstApprovalRequest;
    private REPLY firstApprovalReply;

    private REQUEST secondApprovalRequest;
    private REPLY secondApprovalReply;

    public ClientListLine(REQUEST request, REPLY reply) {
        this.request = request;
        this.reply = reply;
    }

    public ClientListLine(REQUEST request) {
        this.request = request;
        this.reply = null;
    }

    public REQUEST getTravelRefundRequest() {
        return request;
    }

    public REPLY getTravelRefundReply() {
        return reply;
    }
    public void setTravelRefundReply(REPLY reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        String requestString = "", replyString = "";
        if (request == null){
            requestString = "waiting...";
        } else {
            try {
                requestString = (gson.fromJson(((TextMessage)request).getText(), TravelRefundRequest.class)).toString();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        if (reply == null){
            replyString = "waiting...";
        } else {
            try {
                replyString = (gson.fromJson(((TextMessage)reply).getText(), TravelRefundReply.class)).toString();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return requestString + "  --->  " + replyString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientListLine<?, ?> listLine = (ClientListLine<?, ?>) o;
        return request.equals(listLine.request) &&
                Objects.equals(reply, listLine.reply);
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, reply);
    }

    @Override
    public REQUEST getFirstApprovalRequest() {
        return firstApprovalRequest;
    }

    @Override
    public void setFirstApprovalRequest(REQUEST firstApprovalRequest) {
        this.firstApprovalRequest = firstApprovalRequest;
    }

    @Override
    public REQUEST getSecondApprovalRequest() {
        return secondApprovalRequest;
    }

    @Override
    public void setSecondApprovalRequest(REQUEST secondApprovalRequest) {
        this.secondApprovalRequest = secondApprovalRequest;
    }

    @Override
    public REPLY getFirstApprovalReply() {
        return firstApprovalReply;
    }

    @Override
    public void setFirstApprovalReply(REPLY firstApprovalReply) {
        this.firstApprovalReply = firstApprovalReply;
    }

    @Override
    public REPLY getSecondApprovalReply() {
        return secondApprovalReply;
    }

    @Override
    public void setSecondApprovalReply(REPLY secondApprovalReply) {
        this.secondApprovalReply = secondApprovalReply;
    }

}
