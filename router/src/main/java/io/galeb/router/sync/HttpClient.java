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

import io.galeb.core.enums.SystemEnv;
import io.galeb.core.logutils.ErrorLogger;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.asynchttpclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

public class HttpClient {

    public static final String NOT_MODIFIED = "NOT_MODIFIED";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

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

    @SuppressWarnings("FutureReturnValueIgnored")
    public void getResponseBody(String url, String etag, OnCompletedCallBack callBack) {
        try {
            RequestBuilder requestBuilder = new RequestBuilder().setUrl(url)
                    .setHeader(Headers.IF_NONE_MATCH, etag);
            asyncHttpClient.executeRequest(requestBuilder.build(), new AsyncCompletionHandler<Response>() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    if (response.getStatusCode() == 304) {
                        callBack.onCompleted(NOT_MODIFIED);
                    } else {
                        callBack.onCompleted(response.getResponseBody());
                    }
                    return response;
                }

                @Override
                public void onThrowable(Throwable t) {
                    callBack.onCompleted(null);
                    super.onThrowable(t);
                }
            });
        } catch (NullPointerException e) {
            LOGGER.error("Token is NULL (auth problem?)");
            callBack.onCompleted(null);
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
            callBack.onCompleted(null);
        }
    }

    public static String localIpsEncoded() {
        final List<String> ipList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                NetworkInterface localInterface = ifs.nextElement();
                if (!localInterface.isLoopback() && localInterface.isUp()) {
                    Enumeration<InetAddress> ips = localInterface.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        InetAddress ipaddress = ips.nextElement();
                        if (ipaddress instanceof Inet4Address) {
                            ipList.add(ipaddress.getHostAddress());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        String ip = String.join("-", ipList);
        if (ip == null || "".equals(ip)) {
            ip = "undef-" + System.currentTimeMillis();
        }
        return ip.replaceAll("[:%]", "");
    }

    public void post(String url, String etag) {
        String env = SystemEnv.ENVIRONMENT_NAME.getValue();
        String groupId = SystemEnv.GROUP_ID.getValue();
        RequestBuilder requestBuilder = new RequestBuilder().setUrl(url)
                .setMethod(HttpMethod.POST.name())
                .setHeader(Headers.IF_NONE_MATCH, etag)
                .setHeader(Headers.X_GALEB_GROUP_ID, groupId)
                .setHeader(Headers.X_GALEB_ENVIRONMENT, env)
                .setHeader(Headers.X_GALEB_LOCAL_IP, localIpsEncoded())
                .setBody("{\"router\":{\"group_id\":\"" + groupId + "\",\"env\":\"" + env + "\",\"etag\":\"" + etag + "\"}}");
        asyncHttpClient.executeRequest(requestBuilder.build(), new AsyncCompletionHandler<String>() {
            @Override
            public String onCompleted(Response response) throws Exception {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("post onCompleted done");
                }
                return "";
            }

            @Override
            public void onThrowable(Throwable t) {
                LOGGER.error(ExceptionUtils.getStackTrace(t));
            }
        });
    }

    public interface OnCompletedCallBack {
        void onCompleted(String body);
    }
}
