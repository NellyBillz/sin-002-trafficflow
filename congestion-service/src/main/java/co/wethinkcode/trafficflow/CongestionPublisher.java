package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;

public final class CongestionPublisher {

    private final String brokerUrl;
    private final String topicName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CongestionPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
    }

    public void publish(int level) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        try (Connection connection = factory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             MessageProducer producer = session.createProducer(session.createTopic(topicName))) {
            producer.send(session.createTextMessage(
                    objectMapper.writeValueAsString(new CongestionEvent(level))));
        } catch (Exception exception) {
            throw new CongestionPublishException("Unable to publish congestion level", exception);
        }
    }

    record CongestionEvent(int level) {
    }
}
