package io.galeb.router.tests.cucumber;

import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import cucumber.api.java.After;

public class CucumberFeatureDirty extends AbstractTestExecutionListener {
    private static boolean dirtyContext = false;

    @After("@DirtyContextAfter")
    public void afterDirtyContext() {
        dirtyContext = true;
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        if (dirtyContext) {
            testContext.markApplicationContextDirty(HierarchyMode.EXHAUSTIVE);
            testContext.setAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE, true);
            dirtyContext = false;
        }
    }
}
