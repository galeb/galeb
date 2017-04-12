package io.galeb.router.tests.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@Service
@Scope("prototype")
public class HttpClient {

    private final Boolean followRedirect = false;
    private final int timeout = 10000;
    private final AsyncHttpClient asyncHttpClient = asyncHttpClient(config()
            .setFollowRedirect(followRedirect)
            .setKeepAlive(false)
            .setConnectTimeout(timeout)
            .setPooledConnectionIdleTimeout(1)
            .setMaxConnectionsPerHost(1).build());

    public Response get(String url) throws InterruptedException, ExecutionException {
        return asyncHttpClient.prepareGet(url).execute().get();
    }

    public Response execute(RequestBuilder builder) throws InterruptedException, java.util.concurrent.ExecutionException {
        Request request = builder.build();
        ListenableFuture<Response> listenableFuture = asyncHttpClient.executeRequest(request);
        return listenableFuture.get();
    }
}
