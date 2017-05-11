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

package io.galeb.router.configurations;

import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.discovery.ExternalDataService;
import io.galeb.router.handlers.NameVirtualHostDefaultHandler;
import io.galeb.router.handlers.PingHandler;
import io.galeb.router.handlers.ShowVirtualHostCachedHandler;
import io.galeb.router.services.UpdaterService;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameVirtualHostHandlerConfiguration {

    private final HttpHandler nameVirtualHostDefaultHandler;
    private final ManagerClientCache cache;
    private final ExternalDataService externalDataService;
    private final UpdaterService updaterService;

    @Autowired
    public NameVirtualHostHandlerConfiguration(final NameVirtualHostDefaultHandler nameVirtualHostDefaultHandler,
                                               final ManagerClientCache cache,
                                               final ExternalDataService externalDataService,
                                               final UpdaterService updaterService) {
        this.nameVirtualHostDefaultHandler = nameVirtualHostDefaultHandler;
        this.cache = cache;
        this.externalDataService = externalDataService;
        this.updaterService = updaterService;
    }

    @Bean
    NameVirtualHostHandler nameVirtualHostHandler() {
        final NameVirtualHostHandler nameVirtualHostHandler = new NameVirtualHostHandler();
        final PingHandler pingHandler = new PingHandler(cache, externalDataService, updaterService);
        final ShowVirtualHostCachedHandler showVirtualHostCachedHandler = new ShowVirtualHostCachedHandler(cache);
        nameVirtualHostHandler.setDefaultHandler(nameVirtualHostDefaultHandler);
        nameVirtualHostHandler.addHost("__ping__", pingHandler);
        nameVirtualHostHandler.addHost("__cache__", showVirtualHostCachedHandler);
        return nameVirtualHostHandler;
    }

}
