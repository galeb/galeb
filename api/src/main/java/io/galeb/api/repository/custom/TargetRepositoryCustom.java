package io.galeb.api.repository.custom;


import io.galeb.core.entity.Target;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("unused")
public interface TargetRepositoryCustom extends WithRoles {

    Target findOne(Long var1);

    Iterable<Target> findAll(Sort var1);

    Page<Target> findAll(Pageable var1);

    void delete(Long id);
}
