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

/**
 *
 *
 */
package io.galeb.router.handlers;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

import io.galeb.core.logutils.ErrorLogger;
import io.galeb.router.ResponseCodeOnError;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import jodd.util.Wildcard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathGlobHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentMap<PathOrdered, HttpHandler> paths = new ConcurrentSkipListMap<>();

    private HttpHandler defaultHandler = ResponseCodeOnError.RULE_PATH_NOT_FOUND.getHandler();

    private HttpHandler pathGlobHandlerCheck() {
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
            exchange.getResponseSender().send("RULE_PATH_REACHABLE");
        };
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final String path = exchange.getRelativePath();
        if ("/__galeb_rule_path_check__".equals(path)) {
            pathGlobHandlerCheck().handleRequest(exchange);
            return;
        }
        final AtomicBoolean hit = new AtomicBoolean(false);
        paths.forEach((key, handler) -> {
            if (!hit.get()) {
                final String pathKey = key.getPath();
                hit.set(Wildcard.match(path, pathKey));
                if (hit.get()) {
                    try {
                        if (handler != null) {
                            handler.handleRequest(exchange);
                        } else {
                            logger.error("Handler is null");
                        }
                    } catch (Exception e) {
                        ErrorLogger.logError(e, this.getClass());
                    }
                }
            }
        });
        if (!hit.get()) {
            defaultHandler.handleRequest(exchange);
        }
    }

    public synchronized ConcurrentMap<PathOrdered, HttpHandler> getPaths() {
        return paths;
    }

    public synchronized boolean contains(final String path) {
        return paths.containsKey(new PathOrdered(path, 0));
    }

    public synchronized boolean addPath(final String path, int order, final HttpHandler handler) {
        return paths.put(new PathOrdered(path.endsWith("/") && !path.contains("*")? path + "*" : path, order), handler) == null;
    }

    public synchronized boolean removePath(final String path) {
        return paths.remove(new PathOrdered(path, 0)) == null;
    }

    public PathGlobHandler setDefaultHandler(HttpHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }

    public HttpHandler getDefaultHandler() {
        return this.defaultHandler;
    }

    public synchronized void clear() {
        paths.clear();
    }

    public static class PathOrdered implements Comparable<PathOrdered> {
        private final String path;
        private final int order;

        public PathOrdered(String path, int order) {
            this.path = path;
            this.order = order;
        }

        public String getPath() {
            return path;
        }

        public int getOrder() {
            return order;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PathOrdered that = (PathOrdered) o;
            return Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }

        @Override
        public int compareTo(final PathOrdered other) {
            if (other == null) return 1;
            return this.order < other.order ? -1 : this.order > other.order ? 1 : 0;
        }
    }
}
