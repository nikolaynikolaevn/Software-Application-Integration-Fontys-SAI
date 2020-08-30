package approval.gui;

import approval.ApprovalRequestListener;
import approval.TravelBrokerAppGateway;
import approval.model.ApprovalReply;
import approval.model.ApprovalRequest;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApprovalController implements Initializable, ApprovalRequestListener {

    @FXML
    private ListView<ApprovalListLine> lvRequestReply;
    @FXML
    private RadioButton rbApprove;
    @FXML
    private RadioButton rbReject;
    @FXML
    private Button btnSendReply;

    private TravelBrokerAppGateway travelBrokerAppGateway;

    private final String approvalName;

    private final Logger logger =  Logger.getLogger(ApprovalController.class.getName());

    public ApprovalController(String approvalAppName, String approvalRequestQueue) {
        this.approvalName = approvalAppName;
        travelBrokerAppGateway = new TravelBrokerAppGateway(approvalRequestQueue);
        travelBrokerAppGateway.setRequestListener(this);
    }

    private void sendApprovalReply() {
        ApprovalListLine rr = lvRequestReply.getSelectionModel().getSelectedItem();
        boolean approved = rbApprove.isSelected();
        String name = "";
        if (!approved){
            name = approvalName;
        }

        ApprovalReply reply = new ApprovalReply(approved,name);
        if (rr!= null){
             rr.setReply(reply);
            lvRequestReply.refresh();
            ApprovalRequest request = rr.getRequest();

            // @TODO: send reply for the selected request
            travelBrokerAppGateway.sendApproval(request, reply);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleGroup radioButtonsGroup = new ToggleGroup();
        rbApprove.setToggleGroup(radioButtonsGroup);
        rbReject.setToggleGroup(radioButtonsGroup);
        btnSendReply.setOnAction(event -> sendApprovalReply());
        logger.log(Level.INFO, "Started listening for requests.");
    }

    @Override
    public void onRequestReceived(ApprovalRequest request, ApprovalReply reply) {
        System.out.println("received approval request: " + request);
        lvRequestReply.getItems().add(new ApprovalListLine(request, null));
    }
}
