package io.galeb.health.services;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Realm;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@Service
public class HttpClientService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService executor = Executors.newWorkStealingPool();

    private final AsyncHttpClient asyncHttpClient;

    public HttpClientService() {
        asyncHttpClient = asyncHttpClient(config()
                .setFollowRedirect(false)
                .setKeepAlive(true)
                .setConnectTimeout(10000)
                .setPooledConnectionIdleTimeout(10)
                .setSoReuseAddress(true)
                .setMaxConnectionsPerHost(100).build());
    }

    public String getResponseBodyWithToken(String url, String token) throws InterruptedException, ExecutionException {
        RequestBuilder requestBuilder = new RequestBuilder().setUrl(url)
                .setHeader("x-auth-token", token);
        Response response = asyncHttpClient.executeRequest(requestBuilder.build()).get();
        return response.getResponseBody();
    }

    public void patchResponse(String url, String body, String token) {
        executor.submit(() -> {
            RequestBuilder requestBuilder = new RequestBuilder().setUrl(url)
                    .setHeader("x-auth-token", token).setMethod("PATCH")
                    .setBody(body);
            try {
                asyncHttpClient.executeRequest(requestBuilder.build()).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        });
    }

    public String getResponseBodyWithAuth(String user, String pass, String url) throws InterruptedException, ExecutionException {
        RequestBuilder requestTokenBuilder = new RequestBuilder().setUrl(url)
                .setRealm(new Realm.Builder(user, pass).setScheme(Realm.AuthScheme.BASIC).build());
        return asyncHttpClient.executeRequest(requestTokenBuilder).get().getResponseBody();
    }
}
