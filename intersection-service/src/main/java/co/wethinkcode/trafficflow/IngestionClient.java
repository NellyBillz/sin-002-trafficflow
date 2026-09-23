package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public final class IngestionClient {

    private final URI baseUri;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IngestionClient(URI baseUri) {
        this.baseUri = baseUri;
    }

    public List<IntersectionRecord> fetchIntersections() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                baseUri.resolve("/intersections")).GET().build();
        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Ingestion service returned HTTP " + response.statusCode());
        }
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }
}
