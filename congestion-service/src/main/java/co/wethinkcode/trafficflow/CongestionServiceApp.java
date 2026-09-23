package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.util.concurrent.atomic.AtomicInteger;

public class CongestionServiceApp {

    public static void main(String[] args) {
        createApp(new AtomicInteger(0)).start(7022);
    }

    static Javalin createApp(AtomicInteger level) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/congestion", ctx -> ctx.json(new CongestionResponse(level.get())));
        app.put("/congestion", ctx -> {
            CongestionRequest request;
            try {
                request = ctx.bodyAsClass(CongestionRequest.class);
            } catch (Exception exception) {
                ctx.status(400).json(new ErrorResponse("Invalid JSON body"));
                return;
            }
            if (request.level() < 0 || request.level() > 8) {
                ctx.status(400).json(new ErrorResponse("Level must be between 0 and 8"));
                return;
            }
            level.set(request.level());
            ctx.json(new CongestionResponse(level.get()));
        });
        return app;
    }

    record CongestionRequest(int level) {
    }

    record CongestionResponse(int level) {
    }

    record ErrorResponse(String error) {
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
