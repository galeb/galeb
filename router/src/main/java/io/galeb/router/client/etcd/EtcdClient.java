package io.galeb.router.client.etcd;

import com.google.gson.Gson;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.http.HttpMethod;
import org.zalando.boot.etcd.EtcdResponse;

import java.util.concurrent.ExecutionException;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class EtcdClient {

    public static final String ETCD_ROOT_PATH = "/v2/keys";

    private final Gson gson = new Gson();

    private final AsyncHttpClient asyncHttpClient;
    private final String server;

    public EtcdClient(String server) {
        this.server = server;
        final int timeout = 10000;
        asyncHttpClient = asyncHttpClient(config()
                .setFollowRedirect(false)
                .setKeepAlive(true)
                .setConnectTimeout(timeout)
                .setPooledConnectionIdleTimeout(10)
                .setMaxConnectionsPerHost(10).build());
    }

    public EtcdResponse get(String key, boolean recursive) throws ExecutionException, InterruptedException {
        final RequestBuilder requestBuilder = new RequestBuilder();
        final Request request = requestBuilder.setMethod(HttpMethod.GET.toString()).setUrl(server + ETCD_ROOT_PATH + key).build();
        final Response response = asyncHttpClient.executeRequest(request).get();
        final String body = response.getResponseBody();
        return gson.fromJson(body, EtcdResponse.class);
    }
}
