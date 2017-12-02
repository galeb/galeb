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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.enums.SystemEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import static io.galeb.core.logutils.ErrorLogger.logError;

@Component
public class ManagerClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new GsonBuilder()
            .setLenient()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    private final String managerUrl = SystemEnv.MANAGER_URL.getValue();
    private final HttpClient httpClient;

    @Autowired
    public ManagerClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void getVirtualhosts(String envname, String etag, ResultCallBack resultCallBack) {
        HttpClient.OnCompletedCallBack callback = body -> {
            if (body != null) {
                if (HttpClient.NOT_MODIFIED.equals(body)) {
                    resultCallBack.onResult(HttpClient.NOT_MODIFIED);
                } else {
                    try {
                        Virtualhosts virtualhosts = gson.fromJson(body, Virtualhosts.class);
                        resultCallBack.onResult(virtualhosts);
                    } catch (Exception e) {
                        logError(e, this.getClass());
                        resultCallBack.onResult(null);
                    }
                }
            } else {
                resultCallBack.onResult(null);
            }
        };
        httpClient.getResponseBody(managerUrl + SystemEnv.MANAGER_MAP_PATH.getValue() + envname, etag, callback);
    }

    public void register(String etag) {
        httpClient.post(managerUrl + SystemEnv.MANAGER_ROUTERS_PATH.getValue(), etag);
    }

    public interface ResultCallBack {
        void onResult(Object result);
    }

    @SuppressWarnings("unused")
    public static class Virtualhosts implements Serializable {
        private static final long serialVersionUID = 1L;
        public VirtualHost[] virtualhosts;
    }

}
