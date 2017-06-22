import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class Starter {

    public static final int PORT_NUMBER = 8082;
    public static final int RESPONSE_DELAY = 3000;
    public static final int HYSTRIX_TIMEOUT = 1000;

    public static void main(String[] args) {

        WireMockServer server = new WireMockServer(wireMockConfig().port(PORT_NUMBER));
        try {
            startAndMockWiremock(server);

            Command command = new Command();

            System.out.println("start: " + new Date());
            Boolean result = command.execute();
            System.out.println("stop: " + new Date() + " " + result);
        } finally {
            server.stop();
        }
    }

    private static void startAndMockWiremock(WireMockServer server) {
        server.start();

        WireMock.configureFor(PORT_NUMBER);
        stubFor(get(urlEqualTo("/2000"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Hey!")
                        .withFixedDelay(RESPONSE_DELAY)));
    }
}
