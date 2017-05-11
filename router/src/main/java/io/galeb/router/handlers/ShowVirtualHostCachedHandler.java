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
import io.galeb.core.entity.VirtualHost;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

public class ShowVirtualHostCachedHandler implements HttpHandler {

    private static final String HEADER_SHOW_CACHE = "X-Galeb-Show-Cache";

    private final Gson gson = new Gson();
    private final ManagerClientCache cache;

    public ShowVirtualHostCachedHandler(final ManagerClientCache cache) {
        this.cache = cache;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.getRequestHeaders().contains(HEADER_SHOW_CACHE)) {
            String virtualhostStr = exchange.getRequestHeaders().getFirst(HEADER_SHOW_CACHE);
            VirtualHost virtualhost = cache.get(virtualhostStr);
            exchange.setStatusCode(virtualhost != null ? StatusCodes.OK : StatusCodes.NOT_FOUND);
            exchange.getResponseSender().send(virtualhost != null ? gson.toJson(virtualhost) : "{}");
        }
        exchange.endExchange();
    }
}
