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

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;

import io.galeb.router.ResponseCodeOnError;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class VirtualHostHandler implements HttpHandler {

    private final HttpHandler errorHandler = ResponseCodeOnError.VIRTUALHOST_NOT_FOUND.getHandler();
    private final Map<String, HttpHandler> hosts = new ConcurrentHashMap<String, HttpHandler>();

    public VirtualHostHandler() {}

    @Override
    public synchronized void handleRequest(HttpServerExchange exchange) throws Exception {
        final String hostHeader = exchange.getRequestHeaders().getFirst(Headers.HOST);
        if (hostHeader == null) {
            errorHandler.handleRequest(exchange);
            return;
        }

        String host;
        if (hostHeader.contains(":")) { //header can be in host:port format
            host = hostHeader.substring(0, hostHeader.lastIndexOf(":")).toLowerCase(Locale.ENGLISH);
        } else {
            host = hostHeader.toLowerCase(Locale.ENGLISH);
        }

        HttpHandler handler = hosts.get(host);
        if (handler != null) {
            handler.handleRequest(exchange);
            return;
        }

        errorHandler.handleRequest(exchange);
    }

    public synchronized VirtualHostHandler addHost(final String host, final HttpHandler handler) {
        Handlers.handlerNotNull(handler);
        hosts.put(host.toLowerCase(Locale.ENGLISH), handler);
        return this;
    }

    public synchronized VirtualHostHandler removeHost(final String host) {
        hosts.remove(host.toLowerCase(Locale.ENGLISH));
        return this;
    }
}
