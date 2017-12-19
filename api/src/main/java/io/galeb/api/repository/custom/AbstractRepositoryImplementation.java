package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.WithStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;

@NoRepositoryBean
public class AbstractRepositoryImplementation<T extends AbstractEntity> {

    private SimpleJpaRepository<T, Long> simpleJpaRepository;
    private StatusService statusService;

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public void setSimpleJpaRepository(Class<T> klazz, EntityManager entityManager) {
        if (this.simpleJpaRepository != null) return;
        this.simpleJpaRepository = new SimpleJpaRepository<>(klazz, entityManager);
    }

    public T findOne(Long id) {
        T entity = simpleJpaRepository.findOne(id);
        ((WithStatus)entity).setStatus(statusService.status((WithStatus) entity, id));
        return entity;
    }

    public Iterable<T> findAll(Sort sort) {
        Iterable<T> iterable = simpleJpaRepository.findAll(sort);
        for (T entity: iterable) {
            ((WithStatus)entity).setStatus(statusService.status((WithStatus) entity, entity.getId()));
        }
        return iterable;
    }

    public Page<T> findAll(Pageable pageable) {
        Page<T> page = simpleJpaRepository.findAll(pageable);
        for (T entity: page) {
            ((WithStatus)entity).setStatus(statusService.status((WithStatus) entity, entity.getId()));
        }
        return page;
    }
}
