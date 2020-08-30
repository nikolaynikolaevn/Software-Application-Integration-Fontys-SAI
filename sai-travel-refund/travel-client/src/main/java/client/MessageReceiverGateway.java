package client;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class MessageReceiverGateway {

    private Connection connection;
    private Session session;
    protected Destination destination;
    private MessageConsumer consumer;

    public MessageReceiverGateway(String queueName) {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            if (queueName == null || queueName.isEmpty())
                destination = session.createTemporaryQueue();
            else
                destination = session.createQueue(queueName);
            consumer = session.createConsumer(destination);

            connection.start();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    void setListener(MessageListener ml) {
        try {
            consumer.setMessageListener(ml);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
