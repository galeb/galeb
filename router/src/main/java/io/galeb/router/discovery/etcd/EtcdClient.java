/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.router.discovery.etcd;

import com.google.gson.Gson;
import io.galeb.core.enums.SystemEnv;
import io.netty.handler.codec.http.HttpMethod;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

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
        int timeout = Integer.parseInt(SystemEnv.EXTERNALDATA_TIMEOUT.getValue());
        int pooledConnectionIdleTimeout = Integer.parseInt(SystemEnv.EXTERNALDATA_POOL.getValue());
        int maxConnectionsPerHost = Integer.parseInt(SystemEnv.EXTERNALDATA_MAXCONN.getValue());
        asyncHttpClient = asyncHttpClient(config()
                .setIoThreadsCount(1)
                .setSoReuseAddress(true)
                .setTcpNoDelay(true)
                .setFollowRedirect(false)
                .setKeepAlive(true)
                .setConnectTimeout(timeout)
                .setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout)
                .setMaxConnectionsPerHost(maxConnectionsPerHost).build());
    }

    public EtcdResponse get(String key) throws ExecutionException, InterruptedException {
        return get(key, false);
    }

    public EtcdResponse get(String key, boolean recursive) throws ExecutionException, InterruptedException {
        final RequestBuilder requestBuilder = new RequestBuilder();
        final Request request = requestBuilder.setMethod(HttpMethod.GET.toString()).setUrl(server + ETCD_ROOT_PATH + key).build();
        final Response response = asyncHttpClient.executeRequest(request).get();
        final String body = response.getResponseBody();
        return gson.fromJson(body, EtcdResponse.class);
    }

    public EtcdResponse put(EtcdNode node) throws ExecutionException, InterruptedException {
        String key = node.getKey();
        Long ttl = node.getTtl();
        String queryTtl = ttl != null && ttl > 0L ? "?ttl=" + ttl : "";
        String bodyRequest = gson.toJson(node);
        final Response response = asyncHttpClient.preparePut(server + ETCD_ROOT_PATH + key + queryTtl).setBody(bodyRequest).execute().get();
        final String body = response.getResponseBody();
        return gson.fromJson(body, EtcdResponse.class);
    }
}
