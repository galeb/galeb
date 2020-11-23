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

import javax.annotation.PostConstruct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.galeb.core.entity.VirtualHost;
import io.galeb.core.enums.SystemEnv;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.handlers.builder.HandlerBuilder;
import io.galeb.router.sync.HttpClient;
import io.galeb.router.sync.ManagerClient;
import io.undertow.server.handlers.NameVirtualHostHandler;

@Service
@Order(4)
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

    @PostConstruct
    public void postConstruct() {
        sync();
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

            ManagerClient.Virtualhosts virtualhostsFromManager = result instanceof ManagerClient.Virtualhosts
                    ? (ManagerClient.Virtualhosts) result
                    : null;
            if (virtualhostsFromManager == null) {
                logger.error("Virtualhosts Empty. Request problem?");
                count = 0;
                wait.set(false);
                return;
            }

            final List<VirtualHost> virtualhosts = Arrays.stream(virtualhostsFromManager.virtualhosts).map(v -> {
                return v;
            }).collect(Collectors.toList());
            logger.info("Processing " + virtualhosts.size() + " virtualhost(s): Check update initialized");

            new HandlerBuilder().build(virtualhosts, applicationContext, nameVirtualHostHandler, cache);
            updateEtagIfNecessary(virtualhosts);

            logger.info("Processed " + count + " virtualhost(s): Done");
            count = 0;
            wait.set(false);
        };

        String etag = cache.etag();
        //List<VirtualHost> lastCache = new ArrayList<>(cache.values());
        managerClient.register(etag);
        managerClient.getVirtualhosts(envName, etag, resultCallBack);
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
}
