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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.util.UUID;


public class RequestIDHandler implements HttpHandler {

    private final HttpString requestIDHeader;
    private HttpHandler next;

    public RequestIDHandler(final String requestIDHeader, HttpHandler next) {
        this.requestIDHeader = new HttpString(requestIDHeader);
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (!exchange.getRequestHeaders().contains(requestIDHeader)) {
            exchange.getRequestHeaders().add(requestIDHeader, UUID.randomUUID().toString());
        }
        if (next != null) {
            next.handleRequest(exchange);
        }
    }

    public void setNext(HttpHandler next) {
        this.next = next;
    }
}
