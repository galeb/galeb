package io.galeb.api.repository.custom;

import com.google.common.collect.Sets;
import io.galeb.api.repository.EnvironmentRepository;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class VirtualHostRepositoryImpl extends AbstractRepositoryImplementation<VirtualHost> implements VirtualHostRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @PostConstruct
    private void init() {
        setEntityManager(em);
        setSimpleJpaRepository(VirtualHost.class, em);
        setStatusService(statusService);
    }

    @Override
    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return Sets.newHashSet(((VirtualHost)entity).getEnvironments());
    }

    @Override
    protected long getProjectId(Object criteria) {
        VirtualHost virtualHost = null;
        try {
            virtualHost = em.find(VirtualHost.class, ((VirtualHost) criteria).getId());
        } catch (Exception ignored) {}
        if (virtualHost == null) {
            return -1L;
        }
        return virtualHost.getProject().getId();
    }

}
