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

import com.google.common.collect.ImmutableSortedMap;

import io.galeb.core.logutils.ErrorLogger;
import io.galeb.router.ResponseCodeOnError;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import jodd.util.Wildcard;

public class PathGlobHandler implements HttpHandler {

    public static final AttachmentKey<String> RULE_NAME = AttachmentKey.create(String.class);

    private final ImmutableSortedMap<PathOrdered, HttpHandler> paths;

    private HttpHandler defaultHandler = ResponseCodeOnError.RULE_PATH_NOT_FOUND.getHandler();

    public PathGlobHandler(ImmutableSortedMap<PathOrdered, HttpHandler> paths) {
        this.paths = paths;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final String path = exchange.getRelativePath();
        if ("/__galeb_rule_path_check__".equals(path)) {
            exchange.putAttachment(RULE_NAME, "__galeb_rule_path_check__");
            pathGlobHandlerCheck().handleRequest(exchange);
            return;
        }

        if (paths.isEmpty()) {
            ResponseCodeOnError.RULES_EMPTY.getHandler().handleRequest(exchange);
            return;
        }

        for (PathOrdered key : paths.keySet()) {
            final String pathKey = key.getPath();
            final HttpHandler handler = paths.get(key);
            if (Wildcard.match(path, pathKey)) {
                try {
                    exchange.putAttachment(RULE_NAME, pathKey);
                    handler.handleRequest(exchange);
                    return;
                } catch (Exception e) {
                    ErrorLogger.logError(e, this.getClass());
                    break;
                }
            }
        }

        exchange.putAttachment(RULE_NAME, "defaultHandler");
        defaultHandler.handleRequest(exchange);
    }

    private HttpHandler pathGlobHandlerCheck() {
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
            exchange.getResponseSender().send("RULE_PATH_REACHABLE");
        };
    }

    public PathGlobHandler setDefaultHandler(HttpHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }

    public HttpHandler getDefaultHandler() {
        return this.defaultHandler;
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
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            PathOrdered that = (PathOrdered) o;
            return Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }

        @Override
        public int compareTo(final PathOrdered other) {
            if (other == null)
                return -1;
            return this.internalId().compareTo(other.internalId());
        }

        private String internalId() {
            int maskSize = (int) Math.pow(Integer.MAX_VALUE, 0.1) + 10;
            final String orderFormated = String.format("%0" + maskSize + "d", order);
            return orderFormated + path;
        }
    }
}
