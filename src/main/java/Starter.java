import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sun.org.apache.xml.internal.serializer.Encodings;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class Starter {

    public static final int PORT_NUMBER = 8082;
    public static final int RESPONSE_DELAY = 1000;
    public static final int HYSTRIX_TIMEOUT = 10000;

    public static void main(String[] args) {

        WireMockServer server = new WireMockServer(wireMockConfig().port(PORT_NUMBER));
        try {
            startAndMockWiremock(server);

            Command command = new Command();

            System.out.println("start: " + new Date());
            Observable<Boolean> result = command.toObservable();
            System.out.println("readingObservableValue: " + new Date());

//            BehaviorSubject<Boolean> booleanBehaviorSubject = BehaviorSubject.create();
//            result.subscribe(booleanBehaviorSubject);
//            Boolean value = booleanBehaviorSubject.getValue();
//
//
            result.subscribe(next -> System.out.println("stop: " + new Date() + " " + next));

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            HttpGet httpRequest = new HttpGet("http://localhost:" + Starter.PORT_NUMBER + "/2000");
//            HttpClient client = HttpClientBuilder.create()
//                    .build();
//            HttpResponse response = client.execute(httpRequest);
//            StringWriter writer = new StringWriter();
//            IOUtils.copy(response.getEntity().getContent(), writer, CharEncoding.UTF_8);
//            String theString = writer.toString();
//
//            System.out.println(theString);
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
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
                        .withBody("true")
                        .withFixedDelay(RESPONSE_DELAY)));
    }
}
