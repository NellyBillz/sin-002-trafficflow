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
import java.time.Instant;

public final class HeartbeatConsumer {

    private final String brokerUrl;
    private final String queueName;
    private final WatchdogState state;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Connection connection;

    public HeartbeatConsumer(String brokerUrl, String queueName, WatchdogState state) {
        this.brokerUrl = brokerUrl;
        this.queueName = queueName;
        this.state = state;
    }

    public void start() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));
        consumer.setMessageListener(this::receive);
        connection.start();
    }

    private void receive(Message message) {
        if (!(message instanceof TextMessage textMessage)) {
            return;
        }
        try {
            JsonNode body = objectMapper.readTree(textMessage.getText());
            if (!"intersection-service".equals(body.path("service").asText())) {
                return;
            }
            Instant.parse(body.path("sentAt").asText());
            state.recordHeartbeat(Instant.now());
        } catch (Exception exception) {
            System.err.println("Ignored invalid heartbeat: " + exception.getMessage());
        }
    }
}
