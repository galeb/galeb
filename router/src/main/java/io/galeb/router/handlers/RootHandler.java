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

import io.galeb.router.client.ExtendedProxyClient;
import io.galeb.router.handlers.completionListeners.AccessLogCompletionListener;
import io.galeb.router.handlers.completionListeners.StatsdCompletionListener;
import io.galeb.router.ResponseCodeOnError;
import io.galeb.router.SystemEnvs;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class RootHandler implements HttpHandler {

    private final Log logger = LogFactory.getLog(this.getClass());

    private final NameVirtualHostHandler nameVirtualHostHandler;
    private final AccessLogCompletionListener accessLogCompletionListener;
    private final StatsdCompletionListener statsdCompletionListener;

    private final boolean enableAccessLog = Boolean.parseBoolean(SystemEnvs.ENABLE_ACCESSLOG.getValue());
    private final boolean enableStatsd    = Boolean.parseBoolean(SystemEnvs.ENABLE_STATSD.getValue());

    public RootHandler(final NameVirtualHostHandler nameVirtualHostHandler,
                       final AccessLogCompletionListener accessLogCompletionListener,
                       final StatsdCompletionListener statsdCompletionListener) {

        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.accessLogCompletionListener = accessLogCompletionListener;
        this.statsdCompletionListener = statsdCompletionListener;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            if (enableAccessLog) exchange.addExchangeCompleteListener(accessLogCompletionListener);
            if (enableStatsd) exchange.addExchangeCompleteListener(statsdCompletionListener);

            nameVirtualHostHandler.handleRequest(exchange);
        } catch (Exception e) {
            ResponseCodeOnError.ROOT_HANDLER_FAILED.getHandler().handleRequest(exchange);
        }
    }

    public synchronized void forceAllUpdate() {
        nameVirtualHostHandler.getHosts().forEach((virtualhost, handler) -> forceVirtualhostUpdate(virtualhost));
    }

    public synchronized void forceVirtualhostUpdate(String virtualhost) {
        if ("__ping__".equals(virtualhost)) return;
        if (nameVirtualHostHandler.getHosts().containsKey(virtualhost)) {
            logger.warn("[" + virtualhost + "] FORCING UPDATE");
            final HttpHandler handler = nameVirtualHostHandler.getHosts().get(virtualhost);
            if (handler instanceof RuleTargetHandler) {
                HttpHandler ruleTargetNextHandler = ((RuleTargetHandler) handler).getNext();
                if (ruleTargetNextHandler instanceof IPAddressAccessControlHandler) {
                    ruleTargetNextHandler = ((IPAddressAccessControlHandler)ruleTargetNextHandler).getNext();
                }
                if (ruleTargetNextHandler instanceof PathGlobHandler) {
                    final PathGlobHandler pathGlobHandler = (PathGlobHandler) ruleTargetNextHandler;
                    pathGlobHandler.getPaths().forEach((k, poolHandler) -> {
                        final ProxyHandler proxyHandler = ((PoolHandler) poolHandler).getProxyHandler();
                        if (proxyHandler != null) {
                            final ExtendedProxyClient proxyClient = (ExtendedProxyClient) proxyHandler.getProxyClient();
                            proxyClient.removeAllHosts();
                        }
                    });
                    pathGlobHandler.clear();
                }
            }
            nameVirtualHostHandler.removeHost(virtualhost);
        }
    }

    public synchronized void forcePoolUpdate(String poolName) {
        nameVirtualHostHandler.getHosts().entrySet().stream()
                .filter(e -> e.getValue() instanceof RuleTargetHandler).forEach(entryHost ->
        {
            final String virtualhost = entryHost.getKey();
            final HttpHandler handler = ((RuleTargetHandler)entryHost.getValue()).getNext();
            if (handler != null) {
                if (handler instanceof PathGlobHandler) {
                    forcePoolUpdateByPathGlobHandler(poolName, virtualhost, (PathGlobHandler) handler);
                }
                if (handler instanceof IPAddressAccessControlHandler) {
                    forcePoolUpdateByIpAclHandler(poolName, virtualhost, (IPAddressAccessControlHandler) handler);
                }
            }
        });
    }

    private synchronized void forcePoolUpdateByPathGlobHandler(String poolName, String virtualhost, PathGlobHandler handler) {
        handler.getPaths().entrySet().stream().map(Map.Entry::getValue)
                .filter(pathHandler -> pathHandler instanceof PoolHandler &&
                        ((PoolHandler) pathHandler).getPoolname() != null &&
                        ((PoolHandler) pathHandler).getPoolname().equals(poolName))
                .forEach(v -> forceVirtualhostUpdate(virtualhost));
    }

    private synchronized void forcePoolUpdateByIpAclHandler(String poolName, String virtualhost, IPAddressAccessControlHandler handler) {
        forcePoolUpdateByPathGlobHandler(poolName, virtualhost, (PathGlobHandler) handler.getNext());
    }
}
