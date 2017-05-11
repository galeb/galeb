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

import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.discovery.ExternalDataService;
import io.galeb.router.services.UpdaterService;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.concurrent.atomic.AtomicLong;

import static io.galeb.router.handlers.PingHandler.HealthcheckBody.*;

public class PingHandler implements HttpHandler {

    enum HealthcheckBody {
        FAIL,
        EMPTY,
        OUTDATED,
        WORKING
    }

    private static final long OBSOLETE_TIME = 10000;

    private final AtomicLong lastPing = new AtomicLong(0L);
    private final ManagerClientCache cache;
    private final ExternalDataService externalDataService;
    private final UpdaterService updaterService;

    public PingHandler(final ManagerClientCache cache,
                       final ExternalDataService externalDataService,
                       final UpdaterService updaterService) {
        this.cache = cache;
        this.externalDataService = externalDataService;
        this.updaterService = updaterService;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        boolean hasNoUpdate = exchange.getQueryParameters().containsKey("noupdate");
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
        String statusBody = getStatusBody(hasNoUpdate);
        exchange.getResponseSender().send(statusBody);
        if (WORKING.name().equals(statusBody)) {
            externalDataService.register();
        }
        if (!hasNoUpdate) {
            updaterService.sched();
        }
        exchange.endExchange();
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

}
