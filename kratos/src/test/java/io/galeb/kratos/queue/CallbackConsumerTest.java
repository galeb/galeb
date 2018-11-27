package io.galeb.kratos.queue;

import io.galeb.core.entity.*;
import io.galeb.kratos.repository.EnvironmentRepository;
import io.galeb.kratos.repository.HealthStatusRepository;
import io.galeb.kratos.repository.PoolRepository;
import io.galeb.kratos.repository.TargetRepository;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.io.File;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@PropertySource("classpath:application.yml")
@AutoConfigureTestEntityManager
@Transactional
public class CallbackConsumerTest {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private HealthStatusRepository healthStatusRepository;

    @Autowired
    private TargetRepository targetRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Flyway FLYWAY = new Flyway();
    private Target target;

    @Before
    @Commit
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
        Environment env;

        env = new Environment();
        env.setId(Long.valueOf("1"));
        env.setName("env-name");
        entityManager.persist(env);

        BalancePolicy bp = new BalancePolicy();
        bp.setName("name-bp");
        entityManager.persist(bp);

        Project project = new Project();
        project.setName("name-project");
        entityManager.persist(project);

        Pool pool = new Pool();
        pool.setName("name-pool");
        pool.setBalancepolicy(bp);
        pool.setEnvironment(env);
        pool.setProject(project);
        entityManager.persist(pool);

        target = new Target();
        target.setName("http://127.0.0.1:8080");
        target.setPool(pool);
        entityManager.persist(target);
        entityManager.flush();
    }


    @Test
    @Rollback(false)
    public void shouldCreateNewHealthStatus() throws InterruptedException {
        //Arrange

        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setSource("source");
        healthStatus.setStatus(HealthStatus.Status.HEALTHY);
        healthStatus.setStatusDetailed("Detailed");
        healthStatus.setTarget(target);

        System.out.println("ID TARGET CREATED: "+ healthStatus.getTarget().getId());

        //Action
        jmsTemplate.convertAndSend("health-callback", healthStatus);

        Thread.sleep(5000); //TODO Need for await finish the queue. Are there other way for this?

        //Assert
        HealthStatus healthStatusUpdated = healthStatusRepository.findBySourceAndTargetId(healthStatus.getSource(), target.getId());
        Assert.assertTrue(healthStatusUpdated.getStatus().equals(HealthStatus.Status.HEALTHY));
    }

    @Test
    @Ignore
    @Rollback(false)
    public void shouldCreateUpdateHealthStatus() throws InterruptedException {
        //Arrange
        //Arrange

//        Environment env;
//        Target target;
//
//        env = new Environment();
//        env.setId(Long.valueOf("1"));
//        env.setName("env-name");
//        env = entityManager.persist(env);
//
//        BalancePolicy bp = new BalancePolicy();
//        bp.setName("name-bp");
//        bp = entityManager.persist(bp);
//
//        Project project = new Project();
//        project.setName("name-project");
//        project = entityManager.persist(project);
//
//        Pool pool = new Pool();
//        pool.setName("name-pool");
//        pool.setBalancepolicy(bp);
//        pool.setEnvironment(env);
//        pool.setProject(project);
//        pool = entityManager.persist(pool);
//
//        target = new Target();
//        target.setLastModifiedAt(new Date());
//        target.setName("http://127.0.0.1:8080");
//        target.setPool(pool);
//        target = entityManager.persist(target);
//        entityManager.clear();
//
//        HealthStatus healthStatus = new HealthStatus();
//        healthStatus.setSource("source");
//        healthStatus.setStatus(HealthStatus.Status.HEALTHY);
//        healthStatus.setStatusDetailed("Detailed");
//        healthStatus.setTarget(target);
//        healthStatusRepository.saveAndFlush(healthStatus);
//
//        healthStatus.setStatus(HealthStatus.Status.FAIL);
//        healthStatus.setStatusDetailed("Failed to connect");
//
//        //Action
//        jmsTemplate.convertAndSend("health-callback", healthStatus);
//        Thread.sleep(5000); //TODO Need for await finish the queue. Are there other way for this?
//
//        //Assert
//        HealthStatus healthStatusUpdated = healthStatusRepository.findBySourceAndTargetId(healthStatus.getSource(), target.getId());
//        Assert.assertTrue(healthStatusUpdated.getStatus().equals(HealthStatus.Status.FAIL));
//        Assert.assertTrue(healthStatusUpdated.getStatusDetailed().equals("Failed to connect"));
    }

}
