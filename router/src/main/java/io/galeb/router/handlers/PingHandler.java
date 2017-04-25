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

import io.galeb.router.services.ExternalDataService;
import io.galeb.router.services.UpdateService;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PingHandler implements HttpHandler {

    private static final long OBSOLETE_TIME = 5000;
    private final AtomicLong lastPing = new AtomicLong(0L);
    private final ExecutorService executor = new ForkJoinPool();
    private final UpdateService updateService;
    private final ExternalDataService data;

    @Autowired
    public PingHandler(UpdateService updateService, ExternalDataService data) {
        this.updateService = updateService;
        this.data = data;
    }

    boolean isEmpty() {
        return data.listFrom(ExternalDataService.VIRTUALHOSTS_KEY).isEmpty();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String status = (isEmpty() ? "EMPTY" : isOutdated() ? "OUTDATED" : "WORKING");
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
        exchange.getResponseSender().send((data.exist(ExternalDataService.PREFIX_KEY) ?
                status : "FAIL: " + ExternalDataService.PREFIX_KEY + " not found"));
        exchange.endExchange();
        executor.submit(updateService::checkForceUpdateFlag);
    }

    private boolean isOutdated() {
        return lastPing.getAndSet(System.currentTimeMillis()) < System.currentTimeMillis() - OBSOLETE_TIME;
    }
}
