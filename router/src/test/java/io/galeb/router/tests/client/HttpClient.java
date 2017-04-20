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

    private static final boolean KEEP_ALIVE = false;
    private static final boolean FOLLOW_REDIRECT = false;
    private static final int TIMEOUT = 10000;

    private final AsyncHttpClient asyncHttpClient = asyncHttpClient(config()
            .setFollowRedirect(FOLLOW_REDIRECT)
            .setKeepAlive(KEEP_ALIVE)
            .setConnectTimeout(TIMEOUT)
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
