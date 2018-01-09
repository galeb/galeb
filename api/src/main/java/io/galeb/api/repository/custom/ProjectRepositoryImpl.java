package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ProjectRepositoryImpl extends AbstractRepositoryImplementation<Project> implements ProjectRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Project.class, em);
        setStatusService(statusService);
    }

    @Override
    protected long getProjectId(Object criteria) {
        return ((Project) criteria).getId();
    }
}
