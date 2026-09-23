package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.net.URI;

public class RoutingServiceApp {

    private static final String DEFAULT_INTERSECTION_URL = "http://localhost:7021";
    private static final String DEFAULT_CONGESTION_URL = "http://localhost:7022";

    public static void main(String[] args) {
        ServiceClient client = new ServiceClient(
                URI.create(System.getenv().getOrDefault(
                        "INTERSECTION_SERVICE_URL", DEFAULT_INTERSECTION_URL)),
                URI.create(System.getenv().getOrDefault(
                        "CONGESTION_SERVICE_URL", DEFAULT_CONGESTION_URL)));
        createApp(client).start(7023);
    }

    static Javalin createApp(ServiceClient client) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/route", ctx -> {
            String from = ctx.queryParam("from");
            String to = ctx.queryParam("to");
            if (from == null || from.isBlank() || to == null || to.isBlank()) {
                ctx.status(400).json(new ErrorResponse("from and to are required"));
                return;
            }
            double baseMinutes;
            try {
                String rawBase = ctx.queryParam("baseMinutes");
                baseMinutes = rawBase == null ? 10.0 : Double.parseDouble(rawBase);
                if (baseMinutes <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                ctx.status(400).json(new ErrorResponse("baseMinutes must be positive"));
                return;
            }
            try {
                if (!client.intersectionExists(from) || !client.intersectionExists(to)) {
                    ctx.status(404).json(new ErrorResponse("Unknown route intersection"));
                    return;
                }
                int level = client.fetchCongestionLevel();
                int estimate = (int) Math.ceil(baseMinutes * (1.0 + (level * 0.15)));
                ctx.json(new RouteEstimate(from.toUpperCase(), to.toUpperCase(), level, estimate));
            } catch (DependencyException exception) {
                ctx.status(503).json(new ErrorResponse(exception.getMessage()));
            }
        });
        return app;
    }

    record RouteEstimate(String from, String to, int congestionLevel,
                         int estimatedMinutes) {
    }

    record ErrorResponse(String error) {
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
