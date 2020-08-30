package client;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class MessageSenderGateway {

    private Connection connection;
    private Session session;
    private Destination approvalRequestIADestination, approvalRequestFDDestination;
    private MessageProducer approvalRequestIAProducer, approvalRequestFDProducer;

    public MessageSenderGateway() {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            approvalRequestIADestination = session.createQueue("approvalRequestIAQueue");
            approvalRequestIAProducer = session.createProducer(approvalRequestIADestination);

            approvalRequestFDDestination = session.createQueue("approvalRequestFDQueue");
            approvalRequestFDProducer = session.createProducer(approvalRequestFDDestination);

            connection.start();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public Message createTextMessage(String body) {
        try {
            TextMessage msg = session.createTextMessage(body);
            return msg;
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return null;
    }

    void send(Message msg, String destination) {
        try {
            if (destination == "IA")
                approvalRequestIAProducer.send(msg);
            if (destination == "FD")
                approvalRequestFDProducer.send(msg);

            System.out.println("JMSMessageID=" + msg.getJMSMessageID()
                    + " JMSDestination=" + msg.getJMSDestination()
                    + " Text=" + ((TextMessage)msg).getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
