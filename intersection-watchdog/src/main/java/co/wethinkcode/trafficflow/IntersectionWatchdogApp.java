package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import co.wethinkcode.trafficflow.mq.MqConfig;

import java.time.Duration;

public class IntersectionWatchdogApp {

    public static void main(String[] args) throws Exception {
        WatchdogState state = new WatchdogState(Duration.ofSeconds(15));
        HeartbeatConsumer consumer = new HeartbeatConsumer(
                MqConfig.BROKER_URL, MqConfig.HEARTBEAT_QUEUE, state);
        consumer.start();

        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/alert", ctx -> {
            boolean alerting = state.isAlerting();
            AlertResponse response = new AlertResponse(
                    alerting ? "ALERT" : "OK",
                    state.lastHeartbeat(),
                    alerting
                            ? "Intersection service heartbeat is missing"
                            : "Intersection service is responding");
            ctx.status(alerting ? 503 : 200).json(response);
        });
        app.start(7024);
    }

    record AlertResponse(String status, String lastHeartbeat, String message) {
    }
}
