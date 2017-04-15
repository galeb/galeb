
package io.galeb.router.tests.suites;

import io.galeb.router.tests.client.ExtendedLoadBalancingProxyClientTest;
import io.galeb.router.tests.cucumber.CucumberTest;
import io.galeb.router.tests.handlers.ExtendedProxyHandlerTest;
import io.galeb.router.tests.handlers.RootHandlerTest;
import io.galeb.router.tests.hostselectors.HashSourceIpHostSelectorTest;
import io.galeb.router.tests.hostselectors.HashUriPathHostSelectorTest;
import io.galeb.router.tests.hostselectors.LeastConnHostSelectorTest;
import io.galeb.router.tests.hostselectors.LeastConnWithRRHostSelectorTest;
import io.galeb.router.tests.hostselectors.RoundRobinHostSelectorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LeastConnHostSelectorTest.class,
        LeastConnWithRRHostSelectorTest.class,
        RoundRobinHostSelectorTest.class,
        HashUriPathHostSelectorTest.class,
        HashSourceIpHostSelectorTest.class,
        RootHandlerTest.class,
        ExtendedLoadBalancingProxyClientTest.class,
        ExtendedProxyHandlerTest.class,
        CucumberTest.class
})
public class ManagerTestSuite {

}
