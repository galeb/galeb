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

import io.galeb.core.logutils.ErrorLogger;
import org.asynchttpclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.galeb.core.logutils.ErrorLogger.logError;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class HttpClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AsyncHttpClient asyncHttpClient;

    public HttpClient() {
        asyncHttpClient = asyncHttpClient(config()
                .setFollowRedirect(false)
                .setCompressionEnforced(true)
                .setKeepAlive(true)
                .setConnectTimeout(10000)
                .setPooledConnectionIdleTimeout(10)
                .setSoReuseAddress(true)
                .setMaxConnectionsPerHost(100).build());
    }

    public void getResponseBodyWithToken(String url, String token, OnCompletedCallBack callBack) {
        try {
            RequestBuilder requestBuilder = new RequestBuilder().setUrl(url)
                    .setHeader("x-auth-token", token);
            asyncHttpClient.executeRequest(requestBuilder.build(), new AsyncCompletionHandler<Response>() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    callBack.onCompleted(response.getResponseBody());
                    return response;
                }

                @Override
                public void onThrowable(Throwable t) {
                    callBack.onCompleted(null);
                    super.onThrowable(t);
                }
            });
        } catch (NullPointerException e) {
            logger.error("Token is NULL (auth problem?)");
            callBack.onCompleted(null);
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
            callBack.onCompleted(null);
        }
    }

    public String getResponseBodyWithAuth(String user, String pass, String url) {
        RequestBuilder requestTokenBuilder = new RequestBuilder().setUrl(url)
                .setRealm(new Realm.Builder(user, pass).setScheme(Realm.AuthScheme.BASIC).build());
        try {
            Response response = asyncHttpClient.executeRequest(requestTokenBuilder).get();
            if (response.getStatusCode() == 401) {
                logger.error("401 Unauthorized: \"" + user + "\" auth failed");
                return "";
            }
            return response.getResponseBody();
        } catch (Exception e) {
            logError(e, this.getClass());
        }
        return "";
    }

    public interface OnCompletedCallBack {
        void onCompleted(String body);
    }
}
