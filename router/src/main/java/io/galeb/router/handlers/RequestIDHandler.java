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

import io.galeb.core.enums.SystemEnv;
import io.galeb.router.ResponseCodeOnError;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.util.UUID;

public class RequestIDHandler implements HttpHandler {

    private final HttpString requestIdHeader = requestIdHeader();

    private HttpHandler next = null;

    public static HttpString requestIdHeader() {
        return HttpString.tryFromString(System.getProperty("REQUESTID_HEADER", SystemEnv.REQUESTID_HEADER.getValue()));
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (requestIdHeader != null && !exchange.getRequestHeaders().contains(requestIdHeader)) {
            exchange.getRequestHeaders().add(requestIdHeader, UUID.randomUUID().toString());
        }
        if (next != null) {
            next.handleRequest(exchange);
        } else {
            if (exchange.getRequestURI() != null) ResponseCodeOnError.PROXY_HANDLER_NULL.getHandler().handleRequest(exchange);
        }
    }

    public RequestIDHandler setNext(HttpHandler next) {
        this.next = next;
        return this;
    }
}
