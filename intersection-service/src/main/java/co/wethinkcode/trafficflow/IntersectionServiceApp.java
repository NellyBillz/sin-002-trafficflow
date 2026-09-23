package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import co.wethinkcode.trafficflow.mq.MqConfig;

import java.net.URI;

public class IntersectionServiceApp {

    private static final String DEFAULT_INGESTION_URL = "http://localhost:7020";

    public static void main(String[] args) throws Exception {
        String ingestionUrl = System.getenv().getOrDefault(
                "INGESTION_SERVICE_URL", DEFAULT_INGESTION_URL);
        IntersectionDirectory directory = new IntersectionDirectory(
                new IngestionClient(URI.create(ingestionUrl)).fetchIntersections());
        HeartbeatPublisher heartbeatPublisher = new HeartbeatPublisher(
                MqConfig.BROKER_URL, MqConfig.HEARTBEAT_QUEUE);
        heartbeatPublisher.start();
        createApp(directory).start(7021);
    }

    static Javalin createApp(IntersectionDirectory directory) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/intersections", ctx -> ctx.json(directory.intersections()));
        app.get("/intersections/{id}", ctx -> directory.findById(ctx.pathParam("id"))
                .ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(404).json(new ErrorResponse("Unknown intersection"))));
        app.get("/districts/{name}", ctx -> {
            var matches = directory.findByDistrict(ctx.pathParam("name"));
            if (matches.isEmpty()) {
                ctx.status(404).json(new ErrorResponse("Unknown district"));
            } else {
                ctx.json(matches);
            }
        });
        return app;
    }

    record ErrorResponse(String error) {
    }
}
