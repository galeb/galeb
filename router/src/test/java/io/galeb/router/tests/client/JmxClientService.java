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

package io.galeb.router.tests.client;

import io.galeb.core.so.Info;
import io.galeb.router.services.JmxReporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import sun.management.ConnectorAddressLink;

import javax.annotation.PostConstruct;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Profile({ "test" })
@Order(20)
public class JmxClientService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Info info = new Info();
    private final AtomicBoolean enabled = new AtomicBoolean(false);

    private MBeanServerConnection client;

    @PostConstruct
    public void start() throws Exception {
        try {
            String jmxUrl = ConnectorAddressLink.importFrom(Info.getPid());
            if (jmxUrl != null) {
                final JMXServiceURL url = new JMXServiceURL(jmxUrl);
                final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
                client = jmxConn.getMBeanServerConnection();
                enabled.set(true);
            }
        } catch (Exception ignore) {}
    }

    public Long getValue(String name) {
        try {
            final ObjectName mBeanObject = new ObjectName(JmxReporterService.MBEAN_DOMAIN + ":name=" + name);
            return client != null ? (Long)client.getAttribute(mBeanObject, "Value") : 0L;
        } catch (MalformedObjectNameException |IOException | ReflectionException | AttributeNotFoundException | InstanceNotFoundException | MBeanException e) {
            logger.error(e.getMessage());
        }
        return -1L;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

}