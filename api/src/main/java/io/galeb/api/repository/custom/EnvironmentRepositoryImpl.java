package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings("unused")
public class EnvironmentRepositoryImpl extends AbstractRepositoryImplementation<Environment> implements EnvironmentRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Environment.class, em);
        setStatusService(statusService);
    }

    @Override
    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return Collections.singleton((Environment)entity);
    }

    @Override
    public Set<String> roles(Object principal, Object criteria) {
        return Collections.emptySet();
    }
}
