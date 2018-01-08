package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HealthStatusRepositoryImpl extends AbstractRepositoryImplementation<HealthStatus> implements HealthStatusRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(HealthStatus.class, em);
        setStatusService(statusService);
    }

    @Override
    public Set<String> roles(Object principal, Object criteria) {
        return Collections.emptySet();
    }

    @Override
    protected long getProjectId(Object criteria) {
        HealthStatus healthStatus = null;
        try {
            healthStatus = em.find(HealthStatus.class, ((HealthStatus) criteria).getId());
        } catch (Exception ignored) {}
        if (healthStatus == null) {
            return -1L;
        }
        List<Project> projects = em.createQuery("SELECT p FROM Project p INNER JOIN p.pools pools INNER JOIN pools.targets t INNER JOIN t.healthStatus h WHERE h.id = :id", Project.class)
                .setParameter("id", healthStatus.getId())
                .getResultList();
        if (projects == null || projects.isEmpty()) {
            return -1;
        }
        return projects.stream().map(AbstractEntity::getId).findAny().orElse(-1L);
    }
}
