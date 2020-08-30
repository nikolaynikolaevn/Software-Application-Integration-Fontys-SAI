package approval;

import approval.model.ApprovalReply;
import approval.model.ApprovalRequest;

public interface ApprovalRequestListener {
    public void onRequestReceived(ApprovalRequest request, ApprovalReply reply);
}
