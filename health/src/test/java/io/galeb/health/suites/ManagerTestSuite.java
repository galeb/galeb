package io.galeb.health.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.galeb.health.ApplicationTest;
import io.galeb.health.services.HealthCheckServiceTest;
import io.galeb.health.services.HealthCheckServiceUnitTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    HealthCheckServiceUnitTest.class,
    HealthCheckServiceTest.class,
    ApplicationTest.class
})
public class ManagerTestSuite {

}
