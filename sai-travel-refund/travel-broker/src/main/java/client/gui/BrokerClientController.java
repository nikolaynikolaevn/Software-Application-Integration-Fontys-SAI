package client.gui;

import client.model.*;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.glassfish.jersey.client.ClientConfig;

import javax.jms.*;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class BrokerClientController implements Initializable {
    @FXML
    private ListView<MessageHolder<Message, Message>> lvRequestReply;

    Connection connection;
    Session session;

    Destination approvalRequestIADestination, approvalRequestFDDestination, approvalReplyDestination, travelRefundRequestDestination;

    MessageProducer approvalRequestIAProducer, approvalRequestFDProducer, travelRefundReplyProducer;
    MessageConsumer approvalReplyConsumer, travelRefundRequestConsumer;

    Gson gson;

    private ClientConfig config;
    private Client client;
    private URI baseURI;
    private WebTarget serviceTarget;

    private final Logger logger =  Logger.getLogger(BrokerClientController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = connectionFactory.createConnection();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            approvalRequestIADestination = session.createQueue("approvalRequestIAQueue");
            approvalRequestIAProducer = session.createProducer(approvalRequestIADestination);

            approvalRequestFDDestination = session.createQueue("approvalRequestFDQueue");
            approvalRequestFDProducer = session.createProducer(approvalRequestFDDestination);

            approvalReplyDestination = session.createQueue("approvalReplyQueue");
            approvalReplyConsumer = session.createConsumer(approvalReplyDestination);

            travelRefundRequestDestination = session.createQueue("travelRefundRequestQueue");
            travelRefundRequestConsumer = session.createConsumer(travelRefundRequestDestination);

            travelRefundReplyProducer = session.createProducer(null);

            gson = new Gson();

            travelRefundRequestConsumer.setMessageListener(request -> {
                System.out.println("received travel refund request: " + request);
                processTravelRefundRequest(request);
            });

            approvalReplyConsumer.setMessageListener(reply -> {
                System.out.println("received approval reply: " + reply);
                processApprovalReply(reply);
            });

            connection.start();

            logger.log(Level.INFO, "Started listening for replies.");

            config = new ClientConfig();
            client = ClientBuilder.newClient(config);
            baseURI = UriBuilder.fromUri("http://localhost:8080/").build();
            serviceTarget = client.target(baseURI);

        } catch (JMSException e) {
            e.printStackTrace();
            stop();
        }
    }

    public double getPricePerKm() {
        Builder requestBuilder = serviceTarget.path("priceperkm/rest/price")
                .request().accept(MediaType.TEXT_PLAIN);
        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            Double entity = response.readEntity(Double.class);
            System.out.println("Price per kilometer is " + entity);
            return entity;
        } else {
            printError(response);
        }

        return -1;
    }

    private void processTravelRefundRequest(Message request) {
        try {
            /* Receive travel refund request */
            TravelRefundRequest travelRefundRequest = gson.fromJson(((TextMessage) request).getText(), TravelRefundRequest.class);
            lvRequestReply.getItems().add(new ClientListLine<>(request));

            /* Send approval request */
            ApprovalRequest approvalRequest;

            if (travelRefundRequest.getMode() == ClientTravelMode.CAR) {
                double pricePerKm = getPricePerKm();
                double costs = travelRefundRequest.getKilometers() * pricePerKm;
                approvalRequest = new ApprovalRequest(
                        travelRefundRequest.getTeacher(), travelRefundRequest.getStudent(),
                        costs, ApprovalTravelMode.CAR);
            } else {
                approvalRequest = new ApprovalRequest(
                        travelRefundRequest.getTeacher(), travelRefundRequest.getStudent(),
                        travelRefundRequest.getCosts(), ApprovalTravelMode.PUBLIC_TRANSPORT);
            }

            // Get the message holder for the newly stored travel refund request
            MessageHolder<Message, Message> requestReply = getRequestReply(request.getJMSMessageID());

            // Send approval request to the Internship Administration
            Message approvalRequestIA = sendRequestMessage(gson.toJson(approvalRequest), approvalRequestIAProducer);

            // Store the Internship Administration approval request
            requestReply.setFirstApprovalRequest(approvalRequestIA);

            // Send another approval request to the Finance Department if costs >= 50 EUR and store it
            if (approvalRequest.getCosts() >= 50) {
                Message approvalRequestFD = sendRequestMessage(gson.toJson(approvalRequest), approvalRequestFDProducer);
                requestReply.setSecondApprovalRequest(approvalRequestFD);
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void processApprovalReply(Message reply) {
        try {
            /* Receive approval reply */
            MessageHolder<Message, Message> requestReply = getRequestReply(reply.getJMSCorrelationID());

            // check if there is an existing approval request for this approval reply
            // used to get the travel refund list view item and update it by adding the processed travel refund reply
            if (requestReply != null) {

                TravelRefundReply travelRefundReply = null;

                ApprovalRequest firstApprovalRequest = gson.fromJson(((TextMessage) requestReply.getFirstApprovalRequest()).getText(), ApprovalRequest.class);

                /*
                If it is second approval reply and costs >= 50 EUR, then create a travel refund reply and send it
                If costs < 50, then create a travel refund reply and send it after the first approval reply is received
                 */

                /* Send travel refund reply */

                // Check for costs >= 50 EUR to know whether to send the travel refund reply
                if (firstApprovalRequest.getCosts() >= 50) {

                    Message firstApprovalReplyMsg = requestReply.getFirstApprovalReply();

                    // Check if this is the second approval reply and if the replier is a new one
                    if (firstApprovalReplyMsg != null && !firstApprovalReplyMsg.getJMSCorrelationID().equals(reply.getJMSCorrelationID())) {

                        ApprovalReply firstApprovalReply = gson.fromJson(((TextMessage) firstApprovalReplyMsg).getText(), ApprovalReply.class);

                        // Create second approval reply object to be used later for condition checks
                        ApprovalReply secondApprovalReply = gson.fromJson(((TextMessage) reply).getText(), ApprovalReply.class);

                        // Store second approval reply message
                        requestReply.setSecondApprovalReply(reply);

                        // combine rejection reasons of replies
                        String combinedReasonsRejected = "";
                        boolean firstReplyReject = !firstApprovalReply.isApproved();

                        // if first is rejected, add the first reason
                        if (firstReplyReject)
                            combinedReasonsRejected += firstApprovalReply.getReasonRejected();

                        // if second is rejected, add the second reason
                        if (!secondApprovalReply.isApproved()) {

                            // if first is rejected, add a comma as a delimiter
                            if (firstReplyReject)
                                combinedReasonsRejected += ", ";

                            combinedReasonsRejected += secondApprovalReply.getReasonRejected();
                        }

                        travelRefundReply = new TravelRefundReply(
                                // check if previous and new reply are approved and assign final status
                                firstApprovalReply.isApproved() && secondApprovalReply.isApproved(),

                                combinedReasonsRejected,

                                // get the costs of the travel refund request
                                firstApprovalRequest.getCosts());

                        sendReplyMessage(travelRefundReply, requestReply);

                        Platform.runLater(lvRequestReply::refresh);
                    }
                    else
                        requestReply.setFirstApprovalReply(reply);
                } else {
                    // Store first approval reply and send it
                    ApprovalReply firstApprovalReply = gson.fromJson(((TextMessage) reply).getText(), ApprovalReply.class);

                    travelRefundReply = new TravelRefundReply(
                            firstApprovalReply.isApproved(), firstApprovalReply.getReasonRejected(), firstApprovalRequest.getCosts());
                    requestReply.setFirstApprovalReply(reply);
                    sendReplyMessage(travelRefundReply, requestReply);

                    Platform.runLater(lvRequestReply::refresh);
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private Message sendRequestMessage(String body, MessageProducer mp) {
        try {
            TextMessage msg = session.createTextMessage(body);
            mp.send(msg);
            System.out.println("JMSMessageID=" + msg.getJMSMessageID()
                    + " JMSDestination=" + msg.getJMSDestination()
                    + " Text=" + msg.getText());
            return msg;
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendReplyMessage(TravelRefundReply travelRefundReply, MessageHolder<Message, Message> requestReply) {
        try {
            TextMessage replyMsg = session.createTextMessage(gson.toJson(travelRefundReply));

            Message travelRefundRequestMsg = requestReply.getTravelRefundRequest();

            // Set the correlation id of the travel refund reply to the one of the travel refund request
            replyMsg.setJMSCorrelationID(travelRefundRequestMsg.getJMSMessageID());

            // Send the message to the temporary queue for travel refund requests
            travelRefundReplyProducer.send(travelRefundRequestMsg.getJMSReplyTo(), replyMsg);

            // Store the newly sent travel refund reply message
            requestReply.setTravelRefundReply(replyMsg);

            Platform.runLater(lvRequestReply::refresh);

            logger.info("Sent reply " + replyMsg + " for request " + travelRefundRequestMsg);
            System.out.println("JMSMessageID=" + replyMsg.getJMSMessageID()
                    + " JMSDestination=" + replyMsg.getJMSDestination()
                    + " Text=" + replyMsg.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private MessageHolder<Message, Message> getRequestReply(String id) {
        for (MessageHolder<Message, Message> listLine : lvRequestReply.getItems()) {
            try {

                if ((listLine.getTravelRefundRequest() != null && listLine.getTravelRefundRequest().getJMSMessageID().equals(id)) ||
                        (listLine.getFirstApprovalRequest() != null && listLine.getFirstApprovalRequest().getJMSMessageID().equals(id)) ||
                        (listLine.getSecondApprovalRequest() != null && listLine.getSecondApprovalRequest().getJMSMessageID().equals(id)))
                    return listLine;

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * This is executed when the form is closed. See RequesterMain
     */
    public void stop(){
        try {
            if (approvalRequestIAProducer != null) approvalRequestIAProducer.close();
            if (approvalRequestFDProducer != null) approvalRequestFDProducer.close();
            if (approvalReplyConsumer != null) approvalReplyConsumer.close();
            if (travelRefundRequestConsumer != null) travelRefundRequestConsumer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /*
     ****************************************
     * ERROR HANDLING
     ****************************************
     */

    private void printError(Response response) {
        System.out.println("ERROR: " + response);
        String entity = response.readEntity(String.class);
        System.out.println(entity);
    }
}
