package client.gui;

public interface MessageHolder<REQUEST, REPLY> {
    REQUEST getTravelRefundRequest();
    void setTravelRefundReply(REPLY reply);
    REPLY getTravelRefundReply();

    REQUEST getFirstApprovalRequest();
    void setFirstApprovalRequest(REQUEST request);

    REQUEST getSecondApprovalRequest();
    void setSecondApprovalRequest(REQUEST request);

    REPLY getFirstApprovalReply();
    void setFirstApprovalReply(REPLY reply);

    REPLY getSecondApprovalReply();
    void setSecondApprovalReply(REPLY reply);
}
