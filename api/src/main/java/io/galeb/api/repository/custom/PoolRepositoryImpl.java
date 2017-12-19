package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.Pool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SuppressWarnings("unused")
public class PoolRepositoryImpl extends AbstractRepositoryImplementation<Pool> implements PoolRepositoryCustom {

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
    public Pool findOne(Long id) {
        return super.findOne(id);
    }

    @Override
    public Iterable<Pool> findAll(Sort sort) {
        return super.findAll(sort);
    }

    @Override
    public Page<Pool> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}
