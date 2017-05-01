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
import io.galeb.core.logger.ErrorLogger;
import io.galeb.core.rest.ManagerClient;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.services.UpdateService;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.util.Headers;
import org.springframework.http.HttpStatus;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
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
    private final UpdateService updateService;
    private final ManagerClientCache cache;
    private Future<?> taskUpdate = null;

    public PingHandler(final NameVirtualHostHandler nameVirtualHostHandler,
                       final ManagerClient managerClient,
                       final ManagerClientCache cache) {
        this.cache = cache;
        this.updateService = new UpdateService(nameVirtualHostHandler, managerClient, cache);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
        if (exchange.getRequestHeaders().contains(HEADER_SHOW_CACHE)) {
            showVirtualHostCached(exchange);
        } else {
            exchange.getResponseSender().send(getStatusBody());
        }
        exchange.endExchange();
        if (taskUpdate == null || updateService.isDone()) {
            taskUpdate = null;
            taskUpdate = executor.submit(updateService::sync);
        }
    }

    private String getStatusBody() {
        return isEmpty() ? EMPTY.name() : (isOutdated() ? OUTDATED.name() : WORKING.name());
    }

    private boolean isEmpty() {
        return cache.isEmpty();
    }

    private boolean isOutdated() {
        return lastPing.getAndSet(System.currentTimeMillis()) < System.currentTimeMillis() - OBSOLETE_TIME;
    }

    private void showVirtualHostCached(final HttpServerExchange exchange) {
        VirtualHost virtualhost = cache.get(exchange.getRequestHeaders().get(HEADER_SHOW_CACHE).peekFirst());
        exchange.setStatusCode(virtualhost != null ? HttpStatus.OK.value() : HttpStatus.NOT_FOUND.value());
        exchange.getResponseSender().send(virtualhost != null ? gson.toJson(virtualhost) : "{}");
    }
}
