package io.galeb.legba.suites;

import io.galeb.legba.controller.VirtualHostCachedControllerTest;
import io.galeb.legba.conversors.ConverterV1Test;
import io.galeb.legba.services.RoutersServiceTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ConverterV1Test.class,
    RoutersServiceTest.class,
    VirtualHostCachedControllerTest.class
})
public class TestSuite {

}
