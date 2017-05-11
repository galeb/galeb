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

import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.discovery.ExternalDataService;
import io.galeb.router.sync.ManagerClient;
import io.galeb.router.sync.Updater;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class UpdaterService {

    private final AtomicBoolean executeSync = new AtomicBoolean(false);
    private final Updater updater;

    @Autowired
    public UpdaterService(final ManagerClient managerClient,
                          final ManagerClientCache cache,
                          final ExternalDataService externalDataService,
                          final NameVirtualHostHandler nameVirtualHostHandler) {
        updater = new Updater(nameVirtualHostHandler, managerClient, cache, externalDataService);
    }

    @Scheduled(fixedDelay = 2000)
    public void execute() {
        if (executeSync.getAndSet(false)) {
            updater.sync();
        }
    }

    public void sched() {
        executeSync.compareAndSet(false, true);
    }
}
