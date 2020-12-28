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

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import io.galeb.core.enums.SystemEnv;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

@Service
@Order(11)
public class PrometheusService {

    private HTTPServer server;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public PrometheusService() {
        DefaultExports.initialize();
        try {
            server = new HTTPServer(Integer.parseInt(SystemEnv.PROMETHEUS_PORT.getValue()), true);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @PreDestroy
    public void onDestroy() throws Exception {
        this.server.stop();
    }
}
