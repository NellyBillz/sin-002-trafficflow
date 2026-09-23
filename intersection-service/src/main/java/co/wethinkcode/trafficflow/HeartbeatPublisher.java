package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class HeartbeatPublisher {

    private final String brokerUrl;
    private final String queueName;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public HeartbeatPublisher(String brokerUrl, String queueName) {
        this.brokerUrl = brokerUrl;
        this.queueName = queueName;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::publishSafely, 0, 5, TimeUnit.SECONDS);
    }

    private void publishSafely() {
        try {
            publish();
        } catch (Exception exception) {
            System.err.println("Unable to publish intersection heartbeat: "
                    + exception.getMessage());
        }
    }

    private void publish() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        try (Connection connection = factory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             MessageProducer producer = session.createProducer(session.createQueue(queueName))) {
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            String json = objectMapper.writeValueAsString(
                    new Heartbeat("intersection-service", Instant.now().toString()));
            producer.send(session.createTextMessage(json));
        }
    }

    record Heartbeat(String service, String sentAt) {
    }
}
