package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.WithStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.Set;

@NoRepositoryBean
public abstract class AbstractRepositoryImplementation<T extends AbstractEntity> implements WithRoles {

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
        if (entity instanceof WithStatus) {
            entity.setAllEnvironments(getAllEnvironments(entity));
            ((WithStatus) entity).setStatus(statusService.status(entity));
        }
        return entity;
    }

    public Iterable<T> findAll(Sort sort) {
        Iterable<T> iterable = simpleJpaRepository.findAll(sort);
        for (T entity: iterable) {
            if (entity instanceof WithStatus) {
                entity.setAllEnvironments(getAllEnvironments(entity));
                ((WithStatus) entity).setStatus(statusService.status(entity));
            }
        }
        return iterable;
    }

    public Page<T> findAll(Pageable pageable) {
        Page<T> page = simpleJpaRepository.findAll(pageable);
        for (T entity: page) {
            if (entity instanceof WithStatus) {
                entity.setAllEnvironments(getAllEnvironments(entity));
                ((WithStatus) entity).setStatus(statusService.status(entity));
            }
        }
        return page;
    }

    @Transactional
    public void delete(Long id) {
        T entity = simpleJpaRepository.findOne(id);
        entity.quarantine(true);
        simpleJpaRepository.saveAndFlush(entity);
    }

    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return Collections.emptySet();
    }

    @Override
    public abstract boolean hasPermission(Object principal, Object criteria, String role);
}
