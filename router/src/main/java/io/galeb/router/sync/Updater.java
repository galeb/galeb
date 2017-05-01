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
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.entity.util.Cloner;
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

public class Updater {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagerClient managerClient;
    private final ManagerClientCache cache;
    private final NameVirtualHostHandler nameVirtualHostHandler;
    private final Cloner cloner = new Cloner();

    private final String envName = SystemEnv.ENVIRONMENT_NAME.getValue();

    private boolean done = false;
    private long lastDone = 0L;
    private int count = 0;

    public Updater(final NameVirtualHostHandler nameVirtualHostHandler,
                   final ManagerClient managerClient,
                   final ManagerClientCache cache) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.managerClient = managerClient;
        this.cache = cache;
    }

    public synchronized boolean isDone() {
        if (lastDone < System.currentTimeMillis() - 30000L) {
            count = 0;
            done = true;
        }
        return done;
    }

    public synchronized void sync() {
        done = false;
        lastDone = System.currentTimeMillis();
        final ManagerClient.ResultCallBack resultCallBack = result -> {
            FullVirtualhosts virtualhostsFromManager = (FullVirtualhosts) result;
            List<VirtualHost> virtualhosts;
            if (virtualhostsFromManager != null) {
                Set<VirtualHost> aliases = new HashSet<>();
                virtualhosts = Arrays.stream(virtualhostsFromManager._embedded.s)
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
                logger.info("Processing " + virtualhosts.size() + " virtualhost(s): Check update initialized");
                cleanup(virtualhosts);
                virtualhosts.forEach(this::updateCache);
                logger.info("Processed " + count + " virtualhost(s): Done");
            } else {
                logger.error("Virtualhosts Empty. Request problem?");
            }
            count = 0;
            done = true;
        };
        managerClient.getVirtualhosts(envName, resultCallBack);
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

    public void updateCache(VirtualHost virtualHost) {
        String virtualhostName = virtualHost.getName();
        VirtualHost oldVirtualHost = cache.get(virtualhostName);
        if (oldVirtualHost != null) {
            String newFullHash = virtualHost.getProperties().get("fullhash");
            String currentFullhash = oldVirtualHost.getProperties().get("fullhash");
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
        cache.put(virtualhostName, virtualHost);
        expireHandlers(virtualhostName);
        count++;
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
