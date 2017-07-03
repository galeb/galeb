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

package io.galeb.router.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.VirtualHost;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static io.galeb.router.sync.GalebHttpHeaders.X_GALEB_SHOW_CACHE;

@Component
public class ShowVirtualHostCachedHandler implements HttpHandler {

    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final ManagerClientCache cache;

    @Autowired
    public ShowVirtualHostCachedHandler(final ManagerClientCache cache) {
        this.cache = cache;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final String etag = cache.etag();
        if (exchange.getRequestHeaders().contains(X_GALEB_SHOW_CACHE)) {
            String virtualhostStr = exchange.getRequestHeaders().getFirst(X_GALEB_SHOW_CACHE);
            VirtualHost virtualhost = cache.get(virtualhostStr);
            if (virtualhost != null) {
                virtualhost.getEnvironment().getProperties().put("fullhash", etag);
                exchange.setStatusCode(StatusCodes.OK);
                exchange.getResponseSender().send(gson.toJson(virtualhost, VirtualHost.class));
            } else {
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                exchange.getResponseSender().send("{}");
            }
        } else {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("last_hash", cache.etag());
            jsonMap.put("virtualhosts", cache.values());
            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseSender().send(gson.toJson(jsonMap));
        }
        exchange.endExchange();
    }
}
