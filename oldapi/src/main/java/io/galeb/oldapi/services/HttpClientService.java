/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
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

package io.galeb.oldapi.services;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.springframework.stereotype.Service;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@Service
public class HttpClientService {

    private static final String USER_AGENT = "OLDAPI/1.0";
    private boolean followRedirect = true;
    private boolean keepAlive = true;

    public AsyncHttpClient httpClient() {
        DefaultAsyncHttpClientConfig.Builder config = config()
                .setFollowRedirect(followRedirect)
                .setSoReuseAddress(true)
                .setKeepAlive(keepAlive)
                .setUseInsecureTrustManager(true)
                .setUserAgent(USER_AGENT);
        return asyncHttpClient(config);
    }
}
