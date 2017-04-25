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

import io.galeb.router.handlers.NameVirtualHostDefaultHandler;
import io.galeb.router.handlers.PingHandler;
import io.galeb.router.services.ExternalDataService;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameVirtualHostHandlerConfiguration {

    private final HttpHandler nameVirtualHostDefaultHandler;
    private final ExternalDataService data;

    @Autowired
    public NameVirtualHostHandlerConfiguration(final NameVirtualHostDefaultHandler nameVirtualHostDefaultHandler,
                                               final ExternalDataService data) {
        this.nameVirtualHostDefaultHandler = nameVirtualHostDefaultHandler;
        this.data = data;
    }

    @Bean
    NameVirtualHostHandler nameVirtualHostHandler() {
        final NameVirtualHostHandler nameVirtualHostHandler = new NameVirtualHostHandler();
        final PingHandler pingHandler = new PingHandler(nameVirtualHostHandler, data);
        nameVirtualHostHandler.addHost("__ping__", pingHandler).setDefaultHandler(nameVirtualHostDefaultHandler);
        return nameVirtualHostHandler;
    }

}
