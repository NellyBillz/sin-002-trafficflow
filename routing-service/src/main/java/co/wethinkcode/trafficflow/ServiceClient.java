package co.wethinkcode.trafficflow;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class ServiceClient {

    private final URI intersectionUri;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ServiceClient(URI intersectionUri) {
        this.intersectionUri = intersectionUri;
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

    private HttpResponse<String> send(URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
