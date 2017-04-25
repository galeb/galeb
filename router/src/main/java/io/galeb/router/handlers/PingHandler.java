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

import io.galeb.core.rest.ManagerClient;
import io.galeb.router.services.ExternalDataService;
import io.galeb.router.services.UpdateService;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.util.Headers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

import static io.galeb.router.handlers.PingHandler.HealthcheckBody.*;

public class PingHandler implements HttpHandler {

    enum HealthcheckBody {
        FAIL,
        EMPTY,
        OUTDATED,
        WORKING
    }

    private static final long OBSOLETE_TIME = 5000;

    private final AtomicLong lastPing = new AtomicLong(0L);
    private final ExecutorService executor = new ForkJoinPool();
    private final UpdateService updateService;
    private final ExternalDataService data;
    private final ManagerClient managerClient;

    public PingHandler(final NameVirtualHostHandler nameVirtualHostHandler,
                       final ExternalDataService data,
                       final ManagerClient managerClient) {
        this.managerClient = managerClient;
        this.updateService = new UpdateService(nameVirtualHostHandler, data);
        this.data = data;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
        exchange.getResponseSender().send(getStatusBody());
        exchange.endExchange();
        executor.submit(updateService::checkForceUpdateFlag);
    }

    private String getStatusBody() {
        return !data.exist(ExternalDataService.PREFIX_KEY) ? FAIL.name() :
                (isEmpty() ? EMPTY.name() :
                        (isOutdated() ? OUTDATED.name() :
                                WORKING.name()));
    }

    private boolean isEmpty() {
        return managerClient.virtualhostsIsEmpty();
    }

    private boolean isOutdated() {
        return lastPing.getAndSet(System.currentTimeMillis()) < System.currentTimeMillis() - OBSOLETE_TIME;
    }
}
