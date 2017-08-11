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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.galeb.core.entity.Pool;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.logutils.ErrorLogger;
import io.galeb.router.VirtualHostsNotExpired;
import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.galeb.router.handlers.PoolHandler.PROP_CONN_PER_THREAD;
import static io.galeb.router.handlers.PoolHandler.PROP_DISCOVERED_MEMBERS_SIZE;

public class Updater {
    public static final String FULLHASH_PROP = "fullhash";
    public static final String ALIAS_OF      = "alias_of";
    public static final long   WAIT_TIMEOUT  = 10000; // ms

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    private final ManagerClient managerClient;
    private final ManagerClientCache cache;
    private final NameVirtualHostHandler nameVirtualHostHandler;

    private final String envName = SystemEnv.ENVIRONMENT_NAME.getValue();

    private int count = 0;

    public Updater(final NameVirtualHostHandler nameVirtualHostHandler,
                   final ManagerClient managerClient,
                   final ManagerClientCache cache) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.managerClient = managerClient;
        this.cache = cache;
    }

    public void sync() {
        AtomicBoolean wait = new AtomicBoolean(true);
        final ManagerClient.ResultCallBack resultCallBack = result -> {
            if (result instanceof String && HttpClient.NOT_MODIFIED.equals(result)) {
                logger.info("Environment " + envName + ": " + result);
            } else {
                ManagerClient.Virtualhosts virtualhostsFromManager = result instanceof ManagerClient.Virtualhosts ? (ManagerClient.Virtualhosts) result : null;
                if (virtualhostsFromManager != null) {
                    final List<VirtualHost> virtualhosts = processVirtualhostsAndAliases(virtualhostsFromManager);
                    logger.info("Processing " + virtualhosts.size() + " virtualhost(s): Check update initialized");
                    updateEtagAndNumRoutersIfNecessary(virtualhosts);
                    cleanup(virtualhosts);
                    virtualhosts.forEach(this::updateCache);
                    logger.info("Processed " + count + " virtualhost(s): Done");
                } else {
                    logger.error("Virtualhosts Empty. Request problem?");
                }
            }
            count = 0;
            wait.set(false);
        };
        String etag = cache.etag();
        managerClient.register(etag);
        managerClient.getVirtualhosts(envName, etag, resultCallBack);
        // force wait
        long currentWaitTimeOut = System.currentTimeMillis();
        while (wait.get()) {
            if (currentWaitTimeOut < System.currentTimeMillis() - WAIT_TIMEOUT) break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                ErrorLogger.logError(e, this.getClass());
            }
        }
    }

    private void updateEtagAndNumRoutersIfNecessary(final List<VirtualHost> virtualhosts) {
        final String etag;
        final int numRouters;
        if (!virtualhosts.isEmpty()) {
            final VirtualHost virtualhost = virtualhosts.get(0);
            etag = virtualhost.getEnvironment().getProperties().get(FULLHASH_PROP);
            numRouters = virtualhost.getRules().stream()
                            .mapToInt(rule -> {
                                final Pool pool = rule.getPool();
                                if (pool != null) {
                                    final String membersSizeProp = pool.getProperties().get(PROP_DISCOVERED_MEMBERS_SIZE);
                                    return Integer.parseInt(Optional.ofNullable(membersSizeProp).orElse("1"));
                                } else {
                                    return  1;
                                }
                            })
                            .max().orElse(1);
        } else {
            etag = ManagerClientCache.EMPTY;
            numRouters = 1;
        }
        cache.updateEtag(etag);
        updateConnectionsPerThread(numRouters);
    }

    private List<VirtualHost> processVirtualhostsAndAliases(final ManagerClient.Virtualhosts virtualhostsFromManager) {
        final Set<VirtualHost> aliases = new HashSet<>();
        final List<VirtualHost> virtualhosts = Arrays.stream(virtualhostsFromManager.virtualhosts)
                .map(v -> {
                    v.getAliases().forEach(aliasName -> {
                        VirtualHost virtualHostAlias = gson.fromJson(gson.toJson(v), VirtualHost.class);
                        virtualHostAlias.setName(aliasName);
                        virtualHostAlias.getProperties().put(ALIAS_OF, v.getName());
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
        cache.put(virtualhostName, virtualHost);
        expireHandlers(virtualhostName);
        count++;
    }

    private HttpHandler getHostHandler(final String virtualhostName) {
        if (Arrays.stream(VirtualHostsNotExpired.values()).anyMatch(s -> s.getHost().equals(virtualhostName))) return null;
        return nameVirtualHostHandler.getHosts().get(virtualhostName);
    }

    private RuleTargetHandler getRuleTargetHandler(final HttpHandler handler) {
        RuleTargetHandler ruleTargetHandler = null;
        if (handler instanceof RuleTargetHandler) {
            ruleTargetHandler = (RuleTargetHandler)handler;
        }
        if (handler instanceof IPAddressAccessControlHandler) {
            ruleTargetHandler = (RuleTargetHandler) ((IPAddressAccessControlHandler) handler).getNext();
        }
        return ruleTargetHandler;
    }

    private List<PoolHandler> getPoolHandlers(final RuleTargetHandler ruleTargetHandler) {
        final List<PoolHandler> poolHandlers = new ArrayList<>();
        if (ruleTargetHandler != null) {
            poolHandlers.add(ruleTargetHandler.getPoolHandler());
            final PathGlobHandler pathGlobHandler = ruleTargetHandler.getPathGlobHandler();
            pathGlobHandler.getPaths().forEach((k, poolHandler) -> poolHandlers.add((PoolHandler) poolHandler));
        }
        return poolHandlers;
    }

    private ExtendedLoadBalancingProxyClient getExtendedProxyClient(final PoolHandler poolHandler) {
        if (poolHandler != null) {
            final ProxyHandler proxyHandler = poolHandler.getProxyHandler();
            if (proxyHandler != null) {
                return (ExtendedLoadBalancingProxyClient) proxyHandler.getProxyClient();
            } else {
                return null;
            }
        }
        return null;
    }

    private void updateConnectionsPerThread(int numRouters) {
        nameVirtualHostHandler.getHosts().forEach((String key, HttpHandler value) -> {
            final HttpHandler hostHandler = getHostHandler(key);
            if (hostHandler != null) {
                final RuleTargetHandler ruleTargetHandler = getRuleTargetHandler(hostHandler);
                if (ruleTargetHandler != null) {
                    getPoolHandlers(ruleTargetHandler).forEach(p -> {
                        final Pool pool = p.getPool();
                        final ExtendedLoadBalancingProxyClient proxyClient = getExtendedProxyClient(p);
                        int connPerThread = connPerThread(numRouters, pool);
                        proxyClient.setConnectionsPerThread(connPerThread);
                    });
                }
            }
        });
    }

    private int connPerThread(int numRouters, final Pool pool) {
        int poolMaxConn = Integer.parseInt(SystemEnv.POOL_MAXCONN.getValue());
        int connPerThread = poolMaxConn / Integer.parseInt(SystemEnv.IO_THREADS.getValue());
        String propConnPerThread = pool.getProperties().get(PROP_CONN_PER_THREAD);
        if (propConnPerThread != null) {
            try {
                connPerThread = Integer.parseInt(propConnPerThread);
            } catch (NumberFormatException ignore) {}
        }
        float discoveryMembersSize = Math.max((float) numRouters, 1.0f);
        connPerThread = Math.round((float) connPerThread / discoveryMembersSize);
        return connPerThread;
    }

    private void expireHandlers(final String virtualhostName) {
        final HttpHandler hostHandler = getHostHandler(virtualhostName);
        if (hostHandler != null) {
            cleanUpNameVirtualHostHandler(hostHandler);
            nameVirtualHostHandler.removeHost(virtualhostName);
        }
    }

    private void cleanPoolHandler(final PoolHandler poolHandler) {
        final ExtendedProxyClient proxyClient = getExtendedProxyClient(poolHandler);
        if (proxyClient != null) {
            proxyClient.removeAllHosts();
        }
    }

    private void cleanUpNameVirtualHostHandler(final HttpHandler handler) {
        final RuleTargetHandler ruleTargetHandler = getRuleTargetHandler(handler);
        getPoolHandlers(ruleTargetHandler).forEach(this::cleanPoolHandler);
        cleanUpPathGlobHandler(ruleTargetHandler.getPathGlobHandler());
    }

    private void cleanUpPathGlobHandler(final PathGlobHandler pathGlobHandler) {
        if (pathGlobHandler != null) pathGlobHandler.clear();
    }

}
