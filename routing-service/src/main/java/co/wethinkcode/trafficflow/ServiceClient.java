package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class ServiceClient {

    private final URI intersectionUri;
    private final URI congestionUri;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceClient(URI intersectionUri, URI congestionUri) {
        this.intersectionUri = intersectionUri;
        this.congestionUri = congestionUri;
    }

    public boolean intersectionExists(String id) {
        try {
            String encodedId = URLEncoder.encode(id.trim(), StandardCharsets.UTF_8);
            HttpResponse<String> response = send(
                    intersectionUri.resolve("/intersections/" + encodedId));
            if (response.statusCode() == 404) {
                return false;
            }
            if (response.statusCode() != 200) {
                throw new DependencyException("Intersection service unavailable", null);
            }
            return true;
        } catch (DependencyException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DependencyException("Intersection service unavailable", exception);
        }
    }

    public int fetchCongestionLevel() {
        try {
            HttpResponse<String> response = send(congestionUri.resolve("/congestion"));
            if (response.statusCode() != 200) {
                throw new DependencyException("Congestion service unavailable", null);
            }
            JsonNode body = objectMapper.readTree(response.body());
            return body.path("level").asInt();
        } catch (DependencyException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DependencyException("Congestion service unavailable", exception);
        }
    }

    private HttpResponse<String> send(URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
