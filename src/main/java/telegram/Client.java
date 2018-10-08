package telegram;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author alexlovkov
 */
public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final HttpClient httpClient;

    public Client() {
        httpClient = HttpClient.newBuilder().build();
    }

    public void sendMessageToCharm(String message) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://188.166.162.28:8080/charm/" + message.replace(" ", "%20")))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        try {
            HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("sent message to client, status code:{}, body:{}, uri:{}",
                send.statusCode(), send.body(), send);
        } catch (IOException | InterruptedException e) {
            logger.warn("couldn't send message ", e);
        }
    }
}
