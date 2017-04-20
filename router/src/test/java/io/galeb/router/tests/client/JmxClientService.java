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
import java.util.Arrays;

@Service
@Profile({ "test" })
@Order(20)
public class JmxClientService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MBeanServerConnection client;
    private String pidProperty = null;

    @PostConstruct
    public void start() throws Exception {
        if (pidProperty == null) {
            pidProperty = System.getProperty("galeb.pid", "0");
        }
        if ("0".equals(pidProperty)) {
            pidProperty = Arrays.stream(ManagementFactory.getRuntimeMXBean().getName().split("@")).findFirst().orElse("-1");
        }
        if ("-1".equals(pidProperty)) {
            logger.error("PID not found");
            throw new RuntimeException("PID not found");
        }
        int pid = Integer.parseInt(pidProperty);
        String jmxUrl = ConnectorAddressLink.importFrom(pid);
        final JMXServiceURL url = new JMXServiceURL(jmxUrl);
        final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
        client = jmxConn.getMBeanServerConnection();
    }

    public Long getValue(String name) {
        try {
            final ObjectName mBeanObject = new ObjectName(JmxReporterService.MBEAN_DOMAIN + ":name=" + name);
            return (Long)client.getAttribute(mBeanObject, "Value");
        } catch (MalformedObjectNameException |IOException | ReflectionException | AttributeNotFoundException | InstanceNotFoundException | MBeanException e) {
            logger.error(e.getMessage());
        }
        return -1L;
    }

}