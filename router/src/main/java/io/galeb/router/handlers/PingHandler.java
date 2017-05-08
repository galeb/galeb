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
import io.galeb.router.discovery.ExternalDataService;
import io.galeb.router.sync.ManagerClient;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.sync.Updater;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static io.galeb.router.handlers.PingHandler.HealthcheckBody.*;

public class PingHandler implements HttpHandler {

    private static final String HEADER_SHOW_CACHE = "X-Galeb-Show-Cache";

    enum HealthcheckBody {
        FAIL,
        EMPTY,
        OUTDATED,
        WORKING
    }

    private static final long OBSOLETE_TIME = 10000;

    private final Gson gson = new Gson();
    private final AtomicLong lastPing = new AtomicLong(0L);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Updater updater;
    private final ManagerClientCache cache;
    private final ExternalDataService externalDataService;

    private Future<?> taskUpdate = null;

    public PingHandler(final NameVirtualHostHandler nameVirtualHostHandler,
                       final ManagerClient managerClient,
                       final ManagerClientCache cache,
                       final ExternalDataService externalDataService) {
        this.cache = cache;
        this.externalDataService = externalDataService;
        this.updater = new Updater(nameVirtualHostHandler, managerClient, cache, externalDataService);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        boolean hasNoUpdate = exchange.getQueryParameters().containsKey("noupdate");
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
        if (exchange.getRequestHeaders().contains(HEADER_SHOW_CACHE)) {
            showVirtualHostCached(exchange);
        } else {
            String statusBody = getStatusBody(hasNoUpdate);
            exchange.getResponseSender().send(statusBody);
            if (WORKING.name().equals(statusBody)) {
                externalDataService.register();
            }
        }
        exchange.endExchange();
        if (!hasNoUpdate && (taskUpdate == null || updater.isDone() || taskUpdate.isCancelled())) {
            taskUpdate = null;
            taskUpdate = executor.submit(updater::sync);
        }
    }

    private String getStatusBody(boolean hasNoUpdate) {
        return isOutdated(hasNoUpdate) ? OUTDATED.name() : (isEmpty() ? EMPTY.name() : WORKING.name());
    }

    private boolean isEmpty() {
        return cache.isEmpty();
    }

    private boolean isOutdated(boolean hasNoUpdate) {
        long currentObsoleteTime = System.currentTimeMillis() - OBSOLETE_TIME;
        return hasNoUpdate ? lastPing.get() < currentObsoleteTime : lastPing.getAndSet(System.currentTimeMillis()) < currentObsoleteTime;
    }

    private void showVirtualHostCached(final HttpServerExchange exchange) {
        VirtualHost virtualhost = cache.get(exchange.getRequestHeaders().get(HEADER_SHOW_CACHE).peekFirst());
        exchange.setStatusCode(virtualhost != null ? StatusCodes.OK : StatusCodes.NOT_FOUND);
        exchange.getResponseSender().send(virtualhost != null ? gson.toJson(virtualhost) : "{}");
    }
}
