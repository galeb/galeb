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
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Profile({ "test" })
@Order(20)
public class JmxClientService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    private MBeanServerConnection client;

    @PostConstruct
    public void start() throws Exception {
        String pid;
        try {
            String pidProperty = Arrays.stream(ManagementFactory.getRuntimeMXBean().getName().split("@")).findFirst().orElse("-1");
            String pidFromSO = new String(Files.readAllBytes(Paths.get("/proc/self/stat")), Charset.defaultCharset());
            pidFromSO = pidFromSO.split(" ")[0];
            pid = !"-1".equals(pidProperty) ? pidProperty : pidFromSO;
        } catch (IOException e) {
            pid = "-1";
        }
        if ("-1".equals(pid)) {
            logger.error("PID not found");
            return;
        }
        String jmxUrl = ConnectorAddressLink.importFrom(Integer.parseInt(pid));
        if (jmxUrl != null) {
            final JMXServiceURL url = new JMXServiceURL(jmxUrl);
            final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
            client = jmxConn.getMBeanServerConnection();
            enabled.set(true);
        }
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