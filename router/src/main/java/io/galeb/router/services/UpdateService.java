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

import com.google.common.collect.Sets;
import io.galeb.core.configuration.SystemEnvs;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.rest.ManagerClient;
import io.galeb.core.rest.structure.Virtualhosts;
import io.galeb.router.client.ExtendedProxyClient;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.handlers.*;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagerClient managerClient;
    private final ManagerClientCache cache;
    private final NameVirtualHostHandler nameVirtualHostHandler;

    public UpdateService(final NameVirtualHostHandler nameVirtualHostHandler,
                         final ManagerClient managerClient,
                         final ManagerClientCache cache) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.managerClient = managerClient;
        this.cache = cache;
    }

    public void sync() {
        managerClient.getVirtualhosts(result -> {
            Virtualhosts virtualhostsFromManager = (Virtualhosts) result;
            if (virtualhostsFromManager != null) {
                Set<String> virtualhosts = Arrays.stream(virtualhostsFromManager._embedded.virtualhost)
                        .map(AbstractEntity::getName).collect(Collectors.toSet());
                cleanup(virtualhosts);
                virtualhosts.forEach(this::updateCache);
            } else {
                logger.error("Virtualhosts Empty. Request problem?");
            }
        });
    }

    private void cleanup(final Set<String> virtualhosts) {
        synchronized (cache) {
            Set<String> diff = Sets.difference(cache.getAll(), virtualhosts);
            diff.forEach(virtualhostName -> {
                expireHandlers(virtualhostName);
                cache.remove(virtualhostName);
                logger.warn(virtualhostName + ": not exist. Removed");
            });
        }
        Set<String> diff = Sets.difference(nameVirtualHostHandler.getHosts().keySet(), virtualhosts);
        diff.forEach(this::expireHandlers);
    }

    public void updateCache(String virtualhostName) {
        if ("__ping__".equals(virtualhostName)) return;
        managerClient.getVirtualhost(virtualhostName, SystemEnvs.ENVIRONMENT_NAME.getValue(), result -> {
            synchronized (cache) {
                VirtualHost virtualHost = (VirtualHost) result;
                if (virtualHost != null) {
                    VirtualHost oldVirtualHost = cache.get(virtualhostName);
                    if (oldVirtualHost != null) {
                        String fullhash = oldVirtualHost.getProperties().get("fullhash");
                        if (fullhash != null && fullhash.equals(virtualHost.getProperties().get("fullhash"))) {
                            logger.info(virtualhostName + ": not changed.");
                            return;
                        }
                    }
                    cache.put(virtualhostName, virtualHost);
                    expireHandlers(virtualhostName);
                } else {
                    expireHandlers(virtualhostName);
                    cache.remove(virtualhostName);
                }
            }
        });
    }

    private void expireHandlers(String virtualhostName) {
        if ("__ping__".equals(virtualhostName)) return;
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
