package io.galeb.legba.services;

import java.io.File;
import java.util.Date;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;

import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.galeb.core.entity.BalancePolicy;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.Target;
import io.galeb.core.services.ChangesService;
import io.galeb.legba.controller.RoutersController.RouterMeta;

@RunWith(SpringRunner.class)
@SpringBootTest
@PropertySource("classpath:application.yml")
@AutoConfigureTestEntityManager
@Transactional
public class RoutersServiceTest {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Autowired
    private RoutersService routersService;

    @Autowired
    private ChangesService changesService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Flyway FLYWAY = new Flyway();

    @Before
    public void init() {
        String userDir = System.getProperty("user.dir");
        String pathProjectGaleb = userDir.substring(0, userDir.lastIndexOf(File.separator));
        String pathProjectApi = File.separator + pathProjectGaleb +
                                File.separator + "api" +
                                File.separator + "src" +
                                File.separator + "main"+
                                File.separator + "resources" +
                                File.separator + "db" +
                                File.separator + "migration";
        FLYWAY.setLocations("filesystem:" + pathProjectApi);
        FLYWAY.setDataSource(dbUrl, dbUsername, dbPassword);
        FLYWAY.clean();
        FLYWAY.migrate();
        redisTemplate.keys("*").forEach(k -> redisTemplate.delete(k));
    }

    @Test
    public void shouldRegisterRouter() {
        //Arrange
        RouterMeta routerMeta = new RouterMeta();
        routerMeta.groupId = "group-local";
        routerMeta.localIP = "127.0.0.1";
        routerMeta.version = "1";
        routerMeta.envId = "1";
        routerMeta.zoneId = null;

        //Action
        routersService.put(routerMeta);
        Set<JsonSchema.Env> envs = routersService.get(routerMeta.envId);

        //Assert
        JsonSchema.Env env = envs.stream().filter(e -> e.getEnvId().equals(routerMeta.envId)).findAny().orElse(null);
        Assert.assertNotNull(env);
        JsonSchema.GroupID groupID = env.getGroupIDs().stream().filter(g -> g.getGroupID().equals(routerMeta.groupId)).findAny().orElse(null);
        Assert.assertNotNull(groupID);
        boolean containsRouterAndVersion = groupID.getRouters().stream().anyMatch(r -> r.getLocalIp().equals(routerMeta.localIP) && r.getEtag().equals(routerMeta.version));
        Assert.assertTrue(containsRouterAndVersion);
    }

    @Test
    @Rollback(false)
    public void shouldClearChangesWithNewestVersion() throws InterruptedException {
        //Arrange
        RouterMeta routerMeta = new RouterMeta();
        routerMeta.groupId = "group-local";
        routerMeta.localIP = "127.0.0.1";
        routerMeta.actualVersion = "1";
        routerMeta.version = "2";
        routerMeta.envId = "1";
        routerMeta.zoneId = null;
        String versionOldest = "1";
        
        EntityManager em = entityManager.getEntityManager().getEntityManagerFactory().createEntityManager();
        em.setProperty("org.hibernate.flushMode", "Manual");
        em.setFlushMode(FlushModeType.COMMIT);
        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        
        Environment env = new Environment();
        env.setId(Long.valueOf("1"));
        env.setName("env-name");
        em.persist(env);

        BalancePolicy bp = new BalancePolicy();
        bp.setName("name-bp");
        em.persist(bp);

        Project project = new Project();
        project.setName("name-project");
        em.persist(project);

        Pool pool = new Pool();
        pool.setName("name-pool");
        pool.setBalancepolicy(bp);
        pool.setEnvironment(env);
        pool.setProject(project);
        em.persist(pool);

        Target target = new Target();
        target.setLastModifiedAt(new Date());
        target.setName("target-name");
        target.setPool(pool);
        em.persist(target);
        
        em.flush();
        tx.commit();

        tx.begin();
        
        em.createNativeQuery("UPDATE target set quarantine = 1").executeUpdate();
        
        em.flush();
        tx.commit();
        
        em.merge(target);
        
        changesService.register(env, target, versionOldest);

        //Action
        routersService.put(routerMeta);
        em.clear();

        //Thread.sleep(500);
        
        //Assert
        boolean hasChanges = changesService.hasByEnvironmentId(Long.valueOf(routerMeta.envId));
        Assert.assertFalse(hasChanges);
        
        //Thread.sleep(500);
        
        entityManager.clear();
        em.clear();
        
        Target t = em.find(Target.class, 1L);
        Assert.assertNull(t);
    }

    @Test
    public void shouldNotClearChangesWithOldestVersion() {
        //Arrange
        RouterMeta routerMeta = new RouterMeta();
        routerMeta.groupId = "group-local";
        routerMeta.localIP = "127.0.0.1";
        routerMeta.version = "1";
        String versionNewest = "2";
        routerMeta.envId = "1";
        routerMeta.zoneId = null;

        Environment env = new Environment();
        env.setId(Long.valueOf(routerMeta.envId));
        Target target = new Target();
        target.setId(999L);
        target.setLastModifiedAt(new Date());
        changesService.register(env, target, versionNewest);

        //Action
        routersService.put(routerMeta);

        //Assert
        boolean hasChanges = changesService.hasByEnvironmentId(Long.valueOf(routerMeta.envId));
        Assert.assertTrue(hasChanges);
    }

    @Test
    public void shouldRegisterRouterAndAutoRemoveWhenExpires() {
        //Arrange
        RouterMeta routerMeta = new RouterMeta();
        routerMeta.groupId = "group-local";
        routerMeta.localIP = "127.0.0.1";
        routerMeta.version = "1";
        routerMeta.envId = "1";
        routerMeta.zoneId = null;

        routersService.REGISTER_TTL = 1L;

        //Action
        routersService.put(routerMeta);

        //Assert
        try {
            Thread.sleep(5L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Set<JsonSchema.Env> envs = routersService.get(routerMeta.envId);
        JsonSchema.Env env = envs.stream().filter(e -> e.getEnvId().equals(routerMeta.envId)).findAny().orElse(null);
        Assert.assertNull(env);
        
        routersService.REGISTER_TTL = 30000L;
    }
}
