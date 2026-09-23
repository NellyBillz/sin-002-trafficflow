package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.concurrent.atomic.AtomicInteger;

public final class CongestionSubscriber {

    private final String brokerUrl;
    private final String topicName;
    private final AtomicInteger currentLevel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Connection connection;

    public CongestionSubscriber(String brokerUrl, String topicName,
                                AtomicInteger currentLevel) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.currentLevel = currentLevel;
    }

    public void start() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createTopic(topicName));
        consumer.setMessageListener(this::receive);
        connection.start();
    }

    private void receive(Message message) {
        if (!(message instanceof TextMessage textMessage)) {
            return;
        }
        try {
            JsonNode body = objectMapper.readTree(textMessage.getText());
            int level = body.path("level").asInt(-1);
            if (level >= 0 && level <= 8) {
                currentLevel.set(level);
            }
        } catch (Exception exception) {
            System.err.println("Ignored invalid congestion event: " + exception.getMessage());
        }
    }
}
