package telegram;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import javax.imageio.ImageIO;

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

    public static void main(String[] args) {
        Client client = new Client();
        client.sendPhotoAndMessage("hey");
    }

    public void sendMessageToCharm(String message) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://188.166.162.28:8080/charm/" + message.replace(" ", "%20")))
            .timeout(Duration.ofMinutes(1))
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

    public void sendPhotoAndMessage(String message) {
        byte[] photo = getScreenshot();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://188.166.162.28:8080/charm/" + message.replace(" ", "%20")))
            .timeout(Duration.ofMinutes(1))
            .POST(HttpRequest.BodyPublishers.ofByteArray(photo))
            .build();
        try {
            HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("sent message to client, status code:{}, body:{}, uri:{}",
                send.statusCode(), send.body(), send);
        } catch (IOException | InterruptedException e) {
            logger.warn("couldn't send message ", e);
        }
    }

    private byte[] getScreenshot() {
        try {
            BufferedImage screen = Screenshot.get_screen();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screen, "jpg", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (Throwable t) {
            logger.error("couldn't make a screenshot", t);
        }
        return new byte[0];
    }
}
