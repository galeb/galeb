package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SuppressWarnings("unused")
public class EnvironmentRepositoryImpl extends AbstractRepositoryImplementation<Environment> implements EnvironmentRepositoryCustom {

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
    public Environment findOne(Long id) {
        return super.findOne(id);
    }

    @Override
    public Iterable<Environment> findAll(Sort sort) {
        return super.findAll(sort);
    }

    @Override
    public Page<Environment> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}
