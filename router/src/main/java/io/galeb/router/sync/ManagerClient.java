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

package io.galeb.router.sync;

import static io.galeb.core.logutils.ErrorLogger.logError;
import static io.galeb.router.sync.GalebHttpHeaders.X_GALEB_ENVIRONMENT;
import static io.galeb.router.sync.GalebHttpHeaders.X_GALEB_GROUP_ID;
import static io.galeb.router.sync.GalebHttpHeaders.X_GALEB_LOCAL_IP;
import static io.galeb.router.sync.GalebHttpHeaders.X_GALEB_ZONE_ID;
import static io.undertow.util.Headers.IF_NONE_MATCH_STRING;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

import java.io.Serializable;
import java.util.concurrent.Future;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.galeb.core.entity.VirtualHost;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.logutils.ErrorLogger;
import io.galeb.core.so.LocalIP;
import io.netty.handler.codec.http.HttpMethod;

@Component
public class ManagerClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new GsonBuilder().setLenient().serializeNulls()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    private final String managerUrl = SystemEnv.MANAGER_URL.getValue();

    static final String ZONE_ID = SystemEnv.ZONE_ID.getValue();
    static final String GROUP_ID = SystemEnv.GROUP_ID.getValue();
    static final String ENVIRONMENT_NAME = SystemEnv.ENVIRONMENT_NAME.getValue();

    public static final String NOT_MODIFIED = "NOT_MODIFIED";

    private final AsyncHttpClient asyncHttpClient;

    public ManagerClient() {
        asyncHttpClient = asyncHttpClient(config().setFollowRedirect(false).setCompressionEnforced(true)
                .setKeepAlive(true).setConnectTimeout(10000).setPooledConnectionIdleTimeout(10).setSoReuseAddress(true)
                .setMaxConnectionsPerHost(100).build());
    }

    public void getVirtualhosts(String envname, String etag, ResultCallBack resultCallBack) {
        try {
            RequestBuilder requestBuilder = new RequestBuilder()
                    .setUrl(managerUrl + SystemEnv.MANAGER_MAP_PATH.getValue() + envname)
                    .setHeader(X_GALEB_GROUP_ID, ManagerClient.GROUP_ID)
                    .setHeader(X_GALEB_ZONE_ID, ManagerClient.ZONE_ID).setHeader(IF_NONE_MATCH_STRING, etag);
            asyncHttpClient.executeRequest(requestBuilder.build(), new AsyncCompletionHandler<Response>() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    if (response.getStatusCode() == 304) {
                        resultCallBack.onResult(304, null);
                        return response;
                    }

                    try {
                        Virtualhosts virtualhosts = gson.fromJson(response.getResponseBody(), Virtualhosts.class);
                        resultCallBack.onResult(200, virtualhosts);
                    } catch (Exception e) {
                        logError(e, this.getClass());
                        resultCallBack.onResult(500, null);
                    }
                    return response;
                }

                @Override
                public void onThrowable(Throwable t) {
                    resultCallBack.onResult(500, null);
                    super.onThrowable(t);
                }
            });
        } catch (NullPointerException e) {
            logger.error("Token is NULL (auth problem?)");
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }
    }

    public void register(String etag) {
        RequestBuilder requestBuilder = new RequestBuilder()
                .setUrl(managerUrl + SystemEnv.MANAGER_ROUTERS_PATH.getValue()).setMethod(HttpMethod.POST.name())
                .setHeader(IF_NONE_MATCH_STRING, etag).setHeader(X_GALEB_GROUP_ID, ManagerClient.GROUP_ID)
                .setHeader(X_GALEB_ENVIRONMENT, ManagerClient.ENVIRONMENT_NAME)
                .setHeader(X_GALEB_LOCAL_IP, LocalIP.encode()).setHeader(X_GALEB_ZONE_ID, ManagerClient.ZONE_ID)
                .setBody("{\"router\":{\"group_id\":\"" + ManagerClient.GROUP_ID + "\",\"env\":\""
                        + ManagerClient.ENVIRONMENT_NAME + "\",\"etag\":\"" + etag + "\"}}");
        try {
            // Block waiting for response
            Future<Response> whenResponse = asyncHttpClient.executeRequest(requestBuilder.build());
            Response response = whenResponse.get();
            logger.info("Register got: " + response.getStatusCode());
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }

    }

    public interface ResultCallBack {
        void onResult(int status, Virtualhosts result);
    }

    public static class Virtualhosts implements Serializable {
        private static final long serialVersionUID = 1L;
        public VirtualHost[] virtualhosts;
    }

}
