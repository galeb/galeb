package io.galeb.api.repository.custom;


import io.galeb.core.entity.Pool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("unused")
public interface PoolRepositoryCustom {

    Pool findOne(Long var1);

    Iterable<Pool> findAll(Sort var1);

    Page<Pool> findAll(Pageable var1);

    void delete(Long id);
}
