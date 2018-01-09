package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.RoleGroup;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.Set;

public class RoleGroupRepositoryImpl extends AbstractRepositoryImplementation<RoleGroup> implements RoleGroupRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(RoleGroup.class, em);
        setStatusService(statusService);
    }

    @Override
    public Set<String> roles(Object principal, Object criteria) {
        return Collections.emptySet();
    }
}
