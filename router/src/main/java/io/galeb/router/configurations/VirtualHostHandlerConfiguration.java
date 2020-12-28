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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.galeb.router.VirtualHostsNotExpired;
import io.galeb.router.handlers.InfoHandler;
import io.galeb.router.handlers.PingHandler;
import io.galeb.router.handlers.ShowVirtualHostCachedHandler;
import io.galeb.router.handlers.VirtualHostHandler;

@Configuration
public class VirtualHostHandlerConfiguration {

    private final PingHandler pingHandler;
    private final ShowVirtualHostCachedHandler showVirtualHostCachedHandler;
    private final InfoHandler infoHandler;

    @Autowired
    public VirtualHostHandlerConfiguration(final ShowVirtualHostCachedHandler showVirtualHostCachedHandler,
            final PingHandler pingHandler, final InfoHandler infoHandler) {
        this.showVirtualHostCachedHandler = showVirtualHostCachedHandler;
        this.pingHandler = pingHandler;
        this.infoHandler = infoHandler;
    }

    @Bean
    VirtualHostHandler virtualHostHandler() {
        final VirtualHostHandler virtualHostHandler = new VirtualHostHandler();
        virtualHostHandler.addHost(VirtualHostsNotExpired.PING.getHost(), pingHandler);
        virtualHostHandler.addHost(VirtualHostsNotExpired.CACHE.getHost(), showVirtualHostCachedHandler);
        virtualHostHandler.addHost(VirtualHostsNotExpired.INFO.getHost(), infoHandler);
        return virtualHostHandler;
    }

}
