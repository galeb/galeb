package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.RuleOrdered;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SuppressWarnings("unused")
public class RuleOrderedRepositoryImpl extends AbstractRepositoryImplementation<RuleOrdered> implements RuleOrderedRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(RuleOrdered.class, em);
        setStatusService(statusService);
    }

    @Override
    public RuleOrdered findOne(Long id) {
        return super.findOne(id);
    }

    @Override
    public Iterable<RuleOrdered> findAll(Sort sort) {
        return super.findAll(sort);
    }

    @Override
    public Page<RuleOrdered> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}
