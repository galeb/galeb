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
import io.galeb.core.enums.SystemEnv;
import io.galeb.router.sync.structure.FullVirtualhosts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.galeb.core.logutils.ErrorLogger.logError;

@Component
public class ManagerClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();

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
                        FullVirtualhosts virtualhosts = gson.fromJson(body, FullVirtualhosts.class);
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
        httpClient.getResponseBody(managerUrl + "/virtualhostscached/" + envname, etag, callback);
    }

    public void register(String etag) {
        httpClient.head(managerUrl + "/routers", etag);
    }

    public interface ResultCallBack {
        void onResult(Object result);
    }
}
