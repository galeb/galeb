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

package io.galeb.router.sync;

import com.google.common.collect.Sets;
import io.galeb.core.entity.Rule;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.entity.util.Cloner;
import io.galeb.router.discovery.ExternalDataService;
import io.galeb.router.sync.structure.FullVirtualhosts;
import io.galeb.router.client.ExtendedProxyClient;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.handlers.PathGlobHandler;
import io.galeb.router.handlers.PoolHandler;
import io.galeb.router.handlers.RuleTargetHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.galeb.router.handlers.PoolHandler.PROP_DISCOVERED_MEMBERS_SIZE;

public class Updater {
    public static final String FULLHASH_PROP = "fullhash";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagerClient managerClient;
    private final ManagerClientCache cache;
    private final ExternalDataService externalDataService;
    private final NameVirtualHostHandler nameVirtualHostHandler;
    private final Cloner cloner = new Cloner();

    private final String envName = SystemEnv.ENVIRONMENT_NAME.getValue();

    private int discoveredMembersSize = 1;
    private int newDiscoveredMembersSize = 1;

    private boolean done = false;
    private long lastDone = 0L;
    private int count = 0;

    public Updater(final NameVirtualHostHandler nameVirtualHostHandler,
                   final ManagerClient managerClient,
                   final ManagerClientCache cache,
                   final ExternalDataService externalDataService) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.managerClient = managerClient;
        this.cache = cache;
        this.externalDataService = externalDataService;
    }

    public synchronized boolean isDone() {
        if (lastDone < System.currentTimeMillis() - 30000L) {
            count = 0;
            done = true;
        }
        return done;
    }

    public synchronized void sync() {
        newDiscoveredMembersSize = Math.max(externalDataService.members().size(), 1);
        done = false;
        lastDone = System.currentTimeMillis();
        final ManagerClient.ResultCallBack resultCallBack = result -> {
            FullVirtualhosts virtualhostsFromManager = (FullVirtualhosts) result;
            if (virtualhostsFromManager != null) {
                final List<VirtualHost> virtualhosts = processVirtualhostsAndAliases(virtualhostsFromManager);
                logger.info("Processing " + virtualhosts.size() + " virtualhost(s): Check update initialized");
                cleanup(virtualhosts);
                virtualhosts.forEach(this::updateCache);
                logger.info("Processed " + count + " virtualhost(s): Done");
            } else {
                logger.error("Virtualhosts Empty. Request problem?");
            }
            count = 0;
            done = true;
            if (discoveredMembersSize != newDiscoveredMembersSize) {
                logger.warn("DiscoveredMembersSize changed from " + discoveredMembersSize + " to "
                        + newDiscoveredMembersSize + ". Expiring ALL handlers");
            }
            discoveredMembersSize = newDiscoveredMembersSize;
        };
        managerClient.getVirtualhosts(envName, resultCallBack);
    }

    private List<VirtualHost> processVirtualhostsAndAliases(final FullVirtualhosts virtualhostsFromManager) {
        final Set<VirtualHost> aliases = new HashSet<>();
        final List<VirtualHost> virtualhosts = Arrays.stream(virtualhostsFromManager._embedded.s)
                .map(v -> {
                    v.getAliases().forEach(aliasName -> {
                        VirtualHost virtualHostAlias = cloner.copyVirtualHost(v);
                        virtualHostAlias.setName(aliasName);
                        aliases.add(virtualHostAlias);
                    });
                    return v;
                })
                .collect(Collectors.toList());
        virtualhosts.addAll(aliases);
        return virtualhosts;
    }

    private void cleanup(final List<VirtualHost> virtualhosts) {
        final Set<String> virtualhostSet = virtualhosts.stream().map(AbstractEntity::getName).collect(Collectors.toSet());
        synchronized (cache) {
            Set<String> diff = Sets.difference(cache.getAll(), virtualhostSet);
            diff.forEach(virtualhostName -> {
                expireHandlers(virtualhostName);
                cache.remove(virtualhostName);
                logger.warn("Virtualhost " + virtualhostName + " not exist. Removed.");
            });
        }
        Set<String> diff = Sets.difference(nameVirtualHostHandler.getHosts().keySet(), virtualhostSet);
        diff.forEach(this::expireHandlers);
    }

    private void updateCache(VirtualHost virtualHost) {
        String virtualhostName = virtualHost.getName();
        if (newDiscoveredMembersSize == discoveredMembersSize) {
            VirtualHost oldVirtualHost = cache.get(virtualhostName);
            if (oldVirtualHost != null) {
                String newFullHash = virtualHost.getProperties().get(FULLHASH_PROP);
                String currentFullhash = oldVirtualHost.getProperties().get(FULLHASH_PROP);
                if (currentFullhash != null && currentFullhash.equals(newFullHash)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Virtualhost " + virtualhostName + " not changed.");
                    }
                    count++;
                    return;
                } else {
                    logger.warn("Virtualhost " + virtualhostName + " changed. Updating cache.");
                }
            }
        }
        applyPropDiscoveryMembersSize(virtualHost);
        cache.put(virtualhostName, virtualHost);
        expireHandlers(virtualhostName);
        count++;
    }

    private void applyPropDiscoveryMembersSize(final VirtualHost virtualHost) {
        applyPropDiscoveryMembersSize(virtualHost.getRuleDefault());
        virtualHost.getRules().forEach(this::applyPropDiscoveryMembersSize);
    }

    private void applyPropDiscoveryMembersSize(final Rule rule) {
        if (rule != null) {
            rule.getPool().getProperties().put(PROP_DISCOVERED_MEMBERS_SIZE, String.valueOf(newDiscoveredMembersSize));
        }
    }

    private void expireHandlers(String virtualhostName) {
        if ("__ping__".equals(virtualhostName)) return;
        if (nameVirtualHostHandler.getHosts().containsKey(virtualhostName)) {
            logger.warn("Virtualhost " + virtualhostName + ": Rebuilding handlers.");
            cleanUpNameVirtualHostHandler(virtualhostName);
            nameVirtualHostHandler.removeHost(virtualhostName);
        }
    }

    private void cleanUpNameVirtualHostHandler(String virtualhost) {
        final HttpHandler handler = nameVirtualHostHandler.getHosts().get(virtualhost);
        RuleTargetHandler ruleTargetHandler = null;
        if (handler instanceof RuleTargetHandler) {
            ruleTargetHandler = (RuleTargetHandler)handler;
        }
        if (handler instanceof IPAddressAccessControlHandler) {
            ruleTargetHandler = (RuleTargetHandler) ((IPAddressAccessControlHandler) handler).getNext();
        }
        if (ruleTargetHandler != null) {
            cleanUpPathGlobHandler(ruleTargetHandler.getPathGlobHandler());
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
