import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Date;

public class Command extends HystrixCommand<Boolean> {

    protected Command() {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("group"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(10)
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                        .withExecutionTimeoutInMilliseconds(Starter.HYSTRIX_TIMEOUT)));
    }

    @Override
    protected Boolean run() throws Exception {
        System.out.println("startRun: " + new Date());
        HttpGet httpRequest = new HttpGet("http://localhost:" + Starter.PORT_NUMBER + "/2000");
        HttpClient client = HttpClientBuilder.create()
                .build();
        client.execute(httpRequest);

        System.out.println("stopRun: " + new Date());
        return true;
    }

    @Override
    protected Boolean getFallback() {
        System.out.println("We've got a problem: " + getExecutionException());
        return false;
    }
}
