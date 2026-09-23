package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IngestionServiceApp {

    public static void main(String[] args) throws Exception {
        List<IntersectionRecord> intersections = loadIntersections();
        createApp(intersections).start(7020);
    }

    static Javalin createApp(List<IntersectionRecord> intersections) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/intersections", ctx -> ctx.json(intersections));
        return app;
    }

    private static List<IntersectionRecord> loadIntersections() throws Exception {
        InputStream stream = IngestionServiceApp.class.getResourceAsStream(
                "/intersections-legacy.csv");
        if (stream == null) {
            throw new IllegalStateException("intersections-legacy.csv was not found");
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new IntersectionCsvCleaner().clean(reader);
        }
    }
}
