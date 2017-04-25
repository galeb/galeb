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

package io.galeb.router.services;

import io.galeb.router.client.ExtendedProxyClient;
import io.galeb.router.handlers.*;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

import static io.galeb.router.services.ExternalDataService.*;

public class UpdateService {

    private static final String FORCE_UPDATE_FLAG = "/force_update";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExternalDataService data;
    private final NameVirtualHostHandler nameVirtualHostHandler;

    public UpdateService(final NameVirtualHostHandler nameVirtualHostHandler, final ExternalDataService externalData) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.data = externalData;
    }

    public void checkForceUpdateFlag() {
        if (forceUpdateAll()) return;
        forceUpdateByVirtualhost();
        forceUpdateByPool();
    }

    private void forceUpdateByVirtualhost() {
        data.listFrom(VIRTUALHOSTS_KEY).stream()
                .filter(node -> data.exist(node.getKey() + FORCE_UPDATE_FLAG))
                .map(node -> data.node(node.getKey() + FORCE_UPDATE_FLAG).getValue())
                .filter(Objects::nonNull)
                .forEach(this::forceVirtualhostUpdate);
    }

    private void forceUpdateByPool() {
        data.listFrom(POOLS_KEY).stream()
                .filter(node -> data.exist(node.getKey() + FORCE_UPDATE_FLAG))
                .map(node -> Long.parseLong(data.node(node.getKey() + FORCE_UPDATE_FLAG).getValue()))
                .filter(Objects::nonNull)
                .forEach(this::forcePoolUpdate);
    }

    private boolean forceUpdateAll() {
        if (data.exist(PREFIX_KEY + FORCE_UPDATE_FLAG)) {
            this.forceAllUpdate();
            return true;
        }
        return false;
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

    public synchronized void forceVirtualhostUpdate(String virtualhost) {
        if ("__ping__".equals(virtualhost)) return;
        if (nameVirtualHostHandler.getHosts().containsKey(virtualhost)) {
            logger.warn("[" + virtualhost + "] FORCING UPDATE");
            cleanUpNameVirtualHostHandler(virtualhost);
            nameVirtualHostHandler.removeHost(virtualhost);
        }
    }

    public synchronized void forcePoolUpdate(long poolId) {
        nameVirtualHostHandler.getHosts().entrySet().stream()
                .filter(e -> e.getValue() instanceof RuleTargetHandler).forEach(entryHost ->
        {
            final String virtualhost = entryHost.getKey();
            final HttpHandler handler = ((RuleTargetHandler)entryHost.getValue()).getNext();
            if (handler != null) {
                if (handler instanceof PathGlobHandler) {
                    forcePoolUpdateByPathGlobHandler(poolId, virtualhost, (PathGlobHandler) handler);
                }
                if (handler instanceof IPAddressAccessControlHandler) {
                    forcePoolUpdateByIpAclHandler(poolId, virtualhost, (IPAddressAccessControlHandler) handler);
                }
            }
        });
    }

    private synchronized void forcePoolUpdateByPathGlobHandler(long poolId, String virtualhost, PathGlobHandler handler) {
        handler.getPaths().entrySet().stream().map(Map.Entry::getValue)
                .filter(pathHandler -> pathHandler instanceof PoolHandler &&
                        ((PoolHandler) pathHandler).getPool() != null &&
                        ((PoolHandler) pathHandler).getPool().getId() == poolId)
                .forEach(v -> forceVirtualhostUpdate(virtualhost));
    }

    private synchronized void forcePoolUpdateByIpAclHandler(long poolId, String virtualhost, IPAddressAccessControlHandler handler) {
        forcePoolUpdateByPathGlobHandler(poolId, virtualhost, (PathGlobHandler) handler.getNext());
    }

    public synchronized void forceAllUpdate() {
        nameVirtualHostHandler.getHosts().forEach((virtualhost, handler) -> forceVirtualhostUpdate(virtualhost));
    }
}
