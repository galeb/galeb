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

import io.galeb.core.configuration.SystemEnvs;
import io.galeb.router.ResponseCodeOnError;
import io.galeb.router.client.ExtendedProxyClient;
import io.galeb.router.configurations.LocalHolderDataConfiguration;
import io.galeb.router.handlers.completionListeners.AccessLogCompletionListener;
import io.galeb.router.handlers.completionListeners.StatsdCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RootHandler implements HttpHandler {

    public static final int SYNC_LIMIT = 5000;

    private final Log logger = LogFactory.getLog(this.getClass());

    private final NameVirtualHostHandler nameVirtualHostHandler;
    private final AccessLogCompletionListener accessLogCompletionListener;
    private final StatsdCompletionListener statsdCompletionListener;
    private final LocalHolderDataConfiguration.LocalHolderData localHolderData;

    private final boolean enableAccessLog = Boolean.parseBoolean(SystemEnvs.ENABLE_ACCESSLOG.getValue());
    private final boolean enableStatsd    = Boolean.parseBoolean(SystemEnvs.ENABLE_STATSD.getValue());

    public RootHandler(final NameVirtualHostHandler nameVirtualHostHandler,
                       final AccessLogCompletionListener accessLogCompletionListener,
                       final StatsdCompletionListener statsdCompletionListener,
                       final LocalHolderDataConfiguration.LocalHolderData localHolderData) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.accessLogCompletionListener = accessLogCompletionListener;
        this.statsdCompletionListener = statsdCompletionListener;
        this.localHolderData = localHolderData;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (localHolderData.lastUpdate() < System.currentTimeMillis() - SYNC_LIMIT) {
            String virtualhostName = exchange.getHostName();
            expireHandlers(virtualhostName);
        }
        try {
            if (enableAccessLog) exchange.addExchangeCompleteListener(accessLogCompletionListener);
            if (enableStatsd) exchange.addExchangeCompleteListener(statsdCompletionListener);

            nameVirtualHostHandler.handleRequest(exchange);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            ResponseCodeOnError.ROOT_HANDLER_FAILED.getHandler().handleRequest(exchange);
        }
    }

    public void expireHandlers(String virtualhostName) {
        if (nameVirtualHostHandler.getHosts().containsKey(virtualhostName)) {
            logger.warn("[" + virtualhostName + "] FORCING UPDATE");
            cleanUpNameVirtualHostHandler(virtualhostName);
            nameVirtualHostHandler.removeHost(virtualhostName);
        }
    }

    private void cleanUpNameVirtualHostHandler(String virtualhost) {
        final HttpHandler handler = nameVirtualHostHandler.getHosts().get(virtualhost);
        if (handler instanceof RuleTargetHandler) {
            HttpHandler ruleTargetNextHandler = ((RuleTargetHandler) handler).getNext();
            if (ruleTargetNextHandler instanceof IPAddressAccessControlHandler) {
                ruleTargetNextHandler = ((IPAddressAccessControlHandler)ruleTargetNextHandler).getNext();
            }
            if (ruleTargetNextHandler instanceof PathGlobHandler) {
                cleanUpPathGlobHandler((PathGlobHandler) ruleTargetNextHandler);
            }
        }
    }

    private void cleanUpPathGlobHandler(final PathGlobHandler pathGlobHandler) {
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
