package io.galeb.router.tests.cache;

import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.VirtualHost;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.sync.Updater;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ManagerClientCacheTest {

    private final Map<String, VirtualHost> virtualhosts = new HashMap<>();
    private final ManagerClientCache managerClientCache = new ManagerClientCache();
    private final Environment env1 = new Environment("env1");
    private final Project project1 = new Project("project1");
    private final String[] virtualhostNames = {"test1", "test2", "test3", "test4"};
    private final String initialEtag = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        env1.getProperties().put(Updater.FULLHASH_PROP, initialEtag);
        Arrays.stream(virtualhostNames).forEach(name -> virtualhosts.put(name, new VirtualHost(name, env1, project1)));
    }

    @After
    public void cleanUp() {
        virtualhosts.clear();
        managerClientCache.clear();
    }

    @Test
    public void checkInitialHash() {
        virtualhosts.forEach(managerClientCache::put);
        assertThat(managerClientCache.etag(), equalTo(initialEtag));
    }

    @Test
    public void checkNewHash() {
        virtualhosts.forEach(managerClientCache::put);
        String newHash = UUID.randomUUID().toString();
        env1.getProperties().put(Updater.FULLHASH_PROP, newHash);
        managerClientCache.put("otherVirtualhost", new VirtualHost("otherVirtualhost", env1, project1));
        assertThat(managerClientCache.etag(), equalTo(newHash));
    }

    @Test
    public void checkForceNewHash() {
        virtualhosts.forEach(managerClientCache::put);
        String newHash = UUID.randomUUID().toString();
        managerClientCache.updateEtag(newHash);
        assertThat(managerClientCache.etag(), equalTo(newHash));
    }

}
