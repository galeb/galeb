package io.galeb.health.suites;

import java.util.Set;

import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.galeb.health.ApplicationTest;
import io.galeb.health.services.HealthCheckServiceTest;
import io.galeb.health.services.HealthCheckServiceUnitTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ HealthCheckServiceUnitTest.class, HealthCheckServiceTest.class, ApplicationTest.class })
public class ManagerTestSuite {
    private static EmbeddedActiveMQ embeddedBus;

    @BeforeClass
    public static void setUp() throws Exception {
        embeddedBus = new EmbeddedActiveMQ();
        embeddedBus.setConfigResourcePath("broker.xml");
        ActiveMQSecurityManager activeMQSecurityManager = new ActiveMQSecurityManager() {
            public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
                return true;
            }

            public boolean validateUser(String user, String password) {
                return true;
            }
        };
        embeddedBus.setSecurityManager(activeMQSecurityManager);
        embeddedBus.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        embeddedBus.stop();
    }
}
