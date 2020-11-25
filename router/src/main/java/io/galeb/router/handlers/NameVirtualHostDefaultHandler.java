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

import org.springframework.context.ApplicationContext;

import io.galeb.router.ResponseCodeOnError;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.NameVirtualHostHandler;

public class NameVirtualHostDefaultHandler implements HttpHandler {

    private final ApplicationContext context;

    public NameVirtualHostDefaultHandler(final ApplicationContext context) {
        this.context = context;
    }

    @Override
    public synchronized void handleRequest(HttpServerExchange exchange) throws Exception {
        final String hostName = exchange.getHostName();
        final NameVirtualHostHandler nameVirtualHostHandler = context.getBean(NameVirtualHostHandler.class);
        if (nameVirtualHostHandler.getHosts().containsKey(hostName)) {
            nameVirtualHostHandler.handleRequest(exchange);
        } else {
            ResponseCodeOnError.VIRTUALHOST_NOT_FOUND.getHandler().handleRequest(exchange);
        }
    }
}
