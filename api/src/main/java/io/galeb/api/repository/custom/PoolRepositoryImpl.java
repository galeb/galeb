package io.galeb.api.repository.custom;

import com.google.common.collect.Sets;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Pool;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class PoolRepositoryImpl extends AbstractRepositoryImplementation<Pool> implements PoolRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Pool.class, em);
        setStatusService(statusService);
    }

    @Override
    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return Sets.newHashSet(((Pool)entity).getEnvironment());
    }

    @Override
    protected long getProjectId(Object criteria) {
        Pool pool = null;
        if (criteria instanceof Pool) {
            pool = em.find(Pool.class, ((Pool) criteria).getId());
        }
        if (criteria instanceof Long) {
            pool = em.find(Pool.class, criteria);
        }
        if (criteria instanceof String) {
            String query = "SELECT p FROM Project p WHERE p.name = :name";
            pool = em.createQuery(query, Pool.class).setParameter("name", criteria).getSingleResult();
        }
        return pool != null ? pool.getProject().getId() : -1L;
    }
}
