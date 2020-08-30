package client;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class MessageSenderGateway {

    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageProducer producer;

    public MessageSenderGateway(String queueName) {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(queueName);
            producer = session.createProducer(destination);

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

    void send(Message msg) {
        try {
            producer.send(msg);
            System.out.println("JMSMessageID=" + msg.getJMSMessageID()
                    + " JMSDestination=" + msg.getJMSDestination()
                    + " Text=" + ((TextMessage)msg).getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
