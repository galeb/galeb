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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

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
import io.galeb.router.sync.ManagerClient;
import io.undertow.server.handlers.NameVirtualHostHandler;

@Service
@Order(4)
public class UpdaterService {

    private static final Logger logger = LoggerFactory.getLogger(UpdaterService.class);

    private final String envName = SystemEnv.ENVIRONMENT_NAME.getValue();
    private final AtomicBoolean executeSync = new AtomicBoolean(false);
    private final ManagerClient managerClient;
    private final ManagerClientCache cache;
    private final NameVirtualHostHandler nameVirtualHostHandler;

    @Autowired
    private ApplicationContext applicationContext;

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
        final ManagerClient.ResultCallBack resultCallBack = (status, vhsFromManager) -> {
            if (status == 304 || status != 200) {
                logger.info("Environment " + envName + " status: " + status);
                return;
            }

            if (vhsFromManager == null) {
                logger.error("Virtualhosts Empty. Request problem?");
                return;
            }

            final List<VirtualHost> managerVirtualHosts = Lists.newArrayList(vhsFromManager.virtualhosts);
            logger.info("Processing " + managerVirtualHosts.size() + " virtualhost(s): Check update initialized");
            String etag = managerVirtualHosts.get(0).getEnvironment().getProperties().get(FULLHASH_PROP);

            MapDifference<String, VirtualHost> diff = cache
                    .diff(Maps.uniqueIndex(managerVirtualHosts, VirtualHost::getName));
            List<VirtualHost> toRemove = new ArrayList<VirtualHost>(diff.entriesOnlyOnLeft().values());
            List<VirtualHost> virtualHosts = new ArrayList<VirtualHost>(diff.entriesOnlyOnRight().values());
            virtualHosts.addAll(diff.entriesDiffering().values().stream().map(v -> {
                // If old virtual host has alias, remove it now
                // might re-add them later
                // TODO: check if aliases are equal, only remove if changed;
                v.leftValue().getAliases().forEach(alias -> {
                    nameVirtualHostHandler.removeHost(alias);
                });
                return v.rightValue();
            }).collect(Collectors.toList()));

            logger.info("Not processing: " + diff.entriesInCommon().values().size()
                    + " virtualhost(s) already configured.");
            logger.info("Processing " + toRemove.size() + " virtualhost(s) to remove");
            logger.info("Processing " + virtualHosts.size() + " virtualhost(s) to add/update");

            // Remove unused handlers
            toRemove.forEach(vh -> {
                nameVirtualHostHandler.removeHost(vh.getName());
                vh.getAliases().forEach(alias -> {
                    nameVirtualHostHandler.removeHost(alias);
                });
            });

            // Update handlers
            new HandlerBuilder().build(virtualHosts, applicationContext, nameVirtualHostHandler, cache);
            cache.updateEtag(etag);

            logger.info("Processed " + virtualHosts.size() + " virtualhost(s): Done");
        };

        String etag = cache.etag();
        // List<VirtualHost> lastCache = new ArrayList<>(cache.values());
        managerClient.register(etag);
        managerClient.getVirtualhosts(envName, etag, resultCallBack);
    }
}
