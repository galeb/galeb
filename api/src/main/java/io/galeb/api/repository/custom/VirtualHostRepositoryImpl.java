package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.VirtualHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SuppressWarnings("unused")
public class VirtualHostRepositoryImpl extends AbstractRepositoryImplementation<VirtualHost> implements VirtualHostRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(VirtualHost.class, em);
        setStatusService(statusService);
    }

    @Override
    public VirtualHost findOne(Long id) {
        return super.findOne(id);
    }

    @Override
    public Iterable<VirtualHost> findAll(Sort sort) {
        return super.findAll(sort);
    }

    @Override
    public Page<VirtualHost> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}
