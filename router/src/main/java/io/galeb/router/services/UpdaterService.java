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

import static io.galeb.router.configurations.ManagerClientCacheConfiguration.FULLHASH_PROP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.galeb.core.entity.VirtualHost;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.logutils.ErrorLogger;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.handlers.HandlerBuilder;
import io.galeb.router.sync.HttpClient;
import io.galeb.router.sync.ManagerClient;
import io.undertow.server.handlers.NameVirtualHostHandler;

@SuppressWarnings("Duplicates")
@Service
public class UpdaterService {

    private static final Logger logger = LoggerFactory.getLogger(UpdaterService.class);

    public static final String ALIAS_OF = "alias_of";
    public static final long WAIT_TIMEOUT = Long
            .parseLong(Optional.ofNullable(System.getenv("WAIT_TIMEOUT")).orElse("20000")); // ms

    // @formatter:off
    private final Gson gson = new GsonBuilder().serializeNulls()
                                               .setLenient()
                                               .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                               .create();
    // @formatter:on
    private final String envName = SystemEnv.ENVIRONMENT_NAME.getValue();
    private final AtomicBoolean executeSync = new AtomicBoolean(false);
    private final ManagerClient managerClient;
    private final ManagerClientCache cache;
    private final NameVirtualHostHandler nameVirtualHostHandler;

    @Autowired
    private ApplicationContext applicationContext;

    private int count = 0;

    @Autowired
    public UpdaterService(final ManagerClient managerClient, final ManagerClientCache cache,
            final NameVirtualHostHandler nameVirtualHostHandler) {
        this.managerClient = managerClient;
        this.cache = cache;
        this.nameVirtualHostHandler = nameVirtualHostHandler;
    }

    @Scheduled(fixedDelay = 5000)
    public void execute() {
        if (executeSync.getAndSet(false)) {
            sync();
        }
    }

    public void sched() {
        executeSync.compareAndSet(false, true);
    }

    public void sync() {
        AtomicBoolean wait = new AtomicBoolean(true);
        final ManagerClient.ResultCallBack resultCallBack = result -> {
            if (result instanceof String && HttpClient.NOT_MODIFIED.equals(result)) {
                logger.info("Environment " + envName + ": " + result);
                count = 0;
                wait.set(false);
                return;
            }

            ManagerClient.Virtualhosts virtualhostsFromManager = result instanceof ManagerClient.Virtualhosts ? (ManagerClient.Virtualhosts) result : null;
            if (virtualhostsFromManager == null) {
                logger.error("Virtualhosts Empty. Request problem?");
                count = 0;
                wait.set(false);
                return;
            }

            final List<VirtualHost> virtualhosts = processVirtualhostsAndAliases(virtualhostsFromManager);
            logger.info("Processing " + virtualhosts.size() + " virtualhost(s): Check update initialized");
            //cleanup(virtualhosts);
            HandlerBuilder.build(virtualhosts, applicationContext, nameVirtualHostHandler);
            //virtualhosts.forEach(this::updateCache);
            updateEtagIfNecessary(virtualhosts);

            logger.info("Processed " + count + " virtualhost(s): Done");
            count = 0;
            wait.set(false);
        };

        String etag = cache.etag();
        List<VirtualHost> lastCache = new ArrayList<>(cache.values());
        managerClient.register(etag);
        managerClient.getVirtualhosts(envName, etag, resultCallBack);

        // force wait
        long currentWaitTimeOut = System.currentTimeMillis();
        boolean failed = false;
        while (wait.get()) {
            if (currentWaitTimeOut < System.currentTimeMillis() - WAIT_TIMEOUT) {
                failed = true;
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                failed = true;
                ErrorLogger.logError(e, this.getClass());
            }
        }
        if (failed) {
            rollback(lastCache);
        }
    }

    private void rollback(List<VirtualHost> lastCache) {
        //cleanup(lastCache);
        HandlerBuilder.build(lastCache, applicationContext, nameVirtualHostHandler);
        updateEtagIfNecessary(lastCache);
    }

    private void updateEtagIfNecessary(final List<VirtualHost> virtualhosts) {
        final String etag;
        if (!virtualhosts.isEmpty()) {
            etag = virtualhosts.get(0).getEnvironment().getProperties().get(FULLHASH_PROP);
        } else {
            etag = ManagerClientCache.EMPTY;
        }
        cache.updateEtag(etag);
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

    // private void cleanup(final List<VirtualHost> virtualhosts) {
    //     final Set<String> virtualhostSet = virtualhosts.stream().map(AbstractEntity::getName).collect(Collectors.toSet());
    //     synchronized (cache) {
    //         Set<String> diff = Sets.difference(cache.getAll(), virtualhostSet);
    //         diff.forEach(virtualhostName -> {
    //             expireHandlers(virtualhostName);
    //             cache.remove(virtualhostName);
    //             logger.warn("Virtualhost " + virtualhostName + " not exist. Removed.");
    //         });
    //     }
    //     Set<String> diff = Sets.difference(nameVirtualHostHandler.getHosts().keySet(), virtualhostSet);
    //     diff.forEach(this::expireHandlers);
    // }

    // private void updateCache(VirtualHost virtualHost) {
    //     String virtualhostName = virtualHost.getName();
    //     VirtualHost oldVirtualHost = cache.get(virtualhostName);
    //     if (oldVirtualHost != null) {
    //         String newFullHash = virtualHost.getProperties().get(FULLHASH_PROP);
    //         String currentFullhash = oldVirtualHost.getProperties().get(FULLHASH_PROP);
    //         if (currentFullhash != null && currentFullhash.equals(newFullHash)) {
    //             if (logger.isDebugEnabled()) {
    //                 logger.debug("Virtualhost " + virtualhostName + " not changed.");
    //             }
    //             count++;
    //             return;
    //         } else {
    //             logger.warn("Virtualhost " + virtualhostName + " changed. Updating cache.");
    //         }
    //     }
    //     cache.put(virtualhostName, virtualHost);
    //     //expireHandlers(virtualhostName);
    //     count++;
    // }

    // private void expireHandlers(String virtualhostName) {
    //     if (Arrays.stream(VirtualHostsNotExpired.values()).anyMatch(s -> s.getHost().equals(virtualhostName))) return;
    //     if (nameVirtualHostHandler.getHosts().containsKey(virtualhostName)) {
    //         logger.warn("Virtualhost " + virtualhostName + ": Rebuilding handlers.");
    //         nameVirtualHostHandler.removeHost(virtualhostName);
    //     }
    // }

}
