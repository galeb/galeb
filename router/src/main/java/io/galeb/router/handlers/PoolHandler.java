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

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.xnio.OptionMap;

import io.galeb.core.entity.BalancePolicy;
import io.galeb.core.entity.Pool;
import io.galeb.core.enums.SystemEnv;
import io.galeb.router.ResponseCodeOnError;
import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.HostSelectorLookup;
import io.galeb.router.client.hostselectors.RoundRobinHostSelector;
import io.undertow.client.UndertowClient;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.ExclusivityChecker;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

public class PoolHandler implements HttpHandler {

    public static final AttachmentKey<String> POOL_NAME = AttachmentKey.create(String.class);

    private static final String CHECK_RULE_HEADER = "X-Check-Pool";
    private static final String X_POOL_NAME_HEADER = "X-Pool-Name";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProxyHandler proxyHandler;
    private final boolean hostsEmpty;
    private final Pool pool;

    public PoolHandler(final Pool pool, ProxyHandler proxyHandler) {
        this.pool = pool;
        this.proxyHandler = proxyHandler;
        this.hostsEmpty = ((ExtendedLoadBalancingProxyClient) proxyHandler.getProxyClient()).isHostsEmpty();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.putAttachment(POOL_NAME, pool.getName());
        if (exchange.getRequestHeaders().contains(CHECK_RULE_HEADER)) {
            healthcheckPoolHandler().handleRequest(exchange);
            return;
        }
        if (hostsEmpty) {
            ResponseCodeOnError.HOSTS_EMPTY.getHandler().handleRequest(exchange);
            return;
        }
        proxyHandler.handleRequest(exchange);
    }

    public Pool getPool() {
        return pool;
    }

    private HttpHandler healthcheckPoolHandler() {
        return exchange -> {
            logger.warn("detected header " + CHECK_RULE_HEADER);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
            exchange.getResponseHeaders().put(HttpString.tryFromString(X_POOL_NAME_HEADER), pool.getName());
            exchange.getResponseSender().send("POOL_REACHABLE");
        };
    }
}
