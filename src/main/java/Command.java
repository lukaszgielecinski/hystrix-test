import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import java.io.IOException;
import java.util.Date;

public class Command extends HystrixObservableCommand<Boolean> {

    protected Command() {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("group"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(10)
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                        .withExecutionTimeoutInMilliseconds(Starter.HYSTRIX_TIMEOUT)));
    }

    public Observable<Boolean> request(String url) {
        Func0<CloseableHttpAsyncClient> resourceFactory = () -> {
            CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
            client.start();

            System.out.println(Thread.currentThread().getName() + " : Created and started the client.");
            return client;
        };

        Func1<HttpAsyncClient, Observable<Boolean>> observableFactory = (client) -> {
            System.out.println(Thread.currentThread().getName() + " : About to create Observable.");
            return ObservableHttp.createGet(url, client).toObservable()
                    .flatMap(observableResponse -> {
                        System.out.println("here: " + new Date());
                        Observable<Boolean> observable = observableResponse.getContent()
                                .map(bytes -> {
                                    System.out.println("start execution: " + new Date());
                                    Boolean value = Boolean.valueOf(
                                            new String(
                                                    bytes));
                                    System.out.println("stop execution: " + new Date());
                                    return value;
                                });
                        System.out.println("there: " + new Date());
                        return observable;
                    });
        };

        Action1<CloseableHttpAsyncClient> disposeAction = (client) -> {
            System.out.println(Thread.currentThread().getName() + " : Closing the client.");
            try {
                client.close();
            } catch (IOException ignored) {
            }
        };

        return Observable.using(
                resourceFactory,
                observableFactory,
                disposeAction);
    }

    @Override
    protected Observable<Boolean> construct() {
        return request("http://localhost:" + Starter.PORT_NUMBER + "/2000");
    }

    @Override
    protected Observable<Boolean> resumeWithFallback() {
        System.out.println("fallback");
        getExecutionException().printStackTrace();
        return Observable.just(Boolean.FALSE);
    }
}
