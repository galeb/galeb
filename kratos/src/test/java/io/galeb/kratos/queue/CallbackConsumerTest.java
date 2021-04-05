package io.galeb.kratos.queue;

import java.io.File;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.galeb.core.entity.BalancePolicy;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.dto.TargetDTO;

@RunWith(SpringRunner.class)
@SpringBootTest
@PropertySource("classpath:application.yml")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
public class CallbackConsumerTest {
    @Autowired
    private JmsTemplate jmsTemplate;

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private DataSource dataSource;
    
    private static final Flyway FLYWAY = new Flyway();

    @Before 
    public void init() {
    	entityManager.clear();
    	
        String userDir = System.getProperty("user.dir");
        String pathProjectGaleb = userDir.substring(0, userDir.lastIndexOf(File.separator));
        String pathProjectApi = File.separator + pathProjectGaleb + File.separator + "api" + File.separator + "src"
                + File.separator + "main" + File.separator + "resources" + File.separator + "db" + File.separator
                + "migration";
        FLYWAY.setLocations("filesystem:" + pathProjectApi);
        FLYWAY.setDataSource(dataSource);
        FLYWAY.clean();
        FLYWAY.migrate();

        EntityManager em = entityManager.getEntityManagerFactory().createEntityManager();
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
        target.setName("http://127.0.0.1:8080");
        target.setPool(pool);
        em.persist(target);
        tx.commit();
    }
    
    @After
    public void resetContexts() {
    	entityManager.flush();
    	entityManager.clear();
    }
    
    @Test
    public void shouldCreateNewHealthStatus() throws InterruptedException {
        EntityManager em = entityManager.getEntityManagerFactory().createEntityManager();
        em.setProperty("org.hibernate.flushMode", "Manual");
        em.setFlushMode(FlushModeType.AUTO);

        // Arrange
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setSource("source");
        healthStatus.setStatus(HealthStatus.Status.HEALTHY);
        healthStatus.setStatusDetailed("Detailed");

        Target target = (Target) entityManager.find(Target.class, 1L);
        target.setHealthStatus(Collections.singleton(healthStatus));
        TargetDTO targetDTO = new TargetDTO(target);

        // Action
        jmsTemplate.convertAndSend("health-callback", targetDTO);
        
        Thread.sleep(5000);

        // Assert
        HealthStatus healthStatusUpdated = em.find(HealthStatus.class, 1L);
        Assert.assertTrue(healthStatusUpdated.getStatus().equals(HealthStatus.Status.HEALTHY));
    }

    @Test
    public void shouldCreateUpdateHealthStatus() throws InterruptedException { 
		EntityManager em = entityManager.getEntityManagerFactory().createEntityManager();
		em.setProperty("org.hibernate.flushMode", "Manual");
		em.setFlushMode(FlushModeType.COMMIT);
    	
		// Arrange
		Target target = (Target) entityManager.find(Target.class, 1L);
		
		EntityTransaction tx = em.getTransaction();
        tx.begin();
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setSource("source");
        healthStatus.setStatus(HealthStatus.Status.HEALTHY);
        healthStatus.setStatusDetailed("Detailed");
        healthStatus.setTarget(target);
        em.persist(healthStatus);
        em.flush();
        tx.commit();

        healthStatus.setStatus(HealthStatus.Status.FAIL);
        healthStatus.setStatusDetailed("Failed to connect");

        target.setHealthStatus(Collections.singleton(healthStatus));
        TargetDTO targetDTO = new TargetDTO(target);
                
        //Action
        jmsTemplate.convertAndSend("health-callback", targetDTO);
        Thread.sleep(5000); //TODO Need for await finish the queue. Are there other way for this?

        //Assert
        HealthStatus healthStatusUpdated = em.find(HealthStatus.class, 1L);
        Assert.assertTrue(healthStatusUpdated.getStatus().equals(HealthStatus.Status.FAIL));
        Assert.assertTrue(healthStatusUpdated.getStatusDetailed().equals("Failed to connect"));
    }

}
