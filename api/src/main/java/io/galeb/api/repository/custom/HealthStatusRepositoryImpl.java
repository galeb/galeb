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

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
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
            if (criteria instanceof HealthStatus) {
                healthStatus = em.find(HealthStatus.class, ((HealthStatus) criteria).getId());
            }
            if (criteria instanceof Long) {
                healthStatus = em.find(HealthStatus.class, criteria);
            }
        } catch (Exception ignored) {}
        if (healthStatus == null) {
            return -1L;
        }
        List<Project> projects = em.createNamedQuery("projectHealthStatus", Project.class)
                .setParameter("id", healthStatus.getId())
                .getResultList();
        if (projects == null || projects.isEmpty()) {
            return -1;
        }
        return projects.stream().map(AbstractEntity::getId).findAny().orElse(-1L);
    }
}
