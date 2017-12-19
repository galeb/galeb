package io.galeb.api.repository.custom;


import io.galeb.core.entity.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("unused")
public interface EnvironmentRepositoryCustom {

    Environment findOne(Long var1);

    Iterable<Environment> findAll(Sort var1);

    Page<Environment> findAll(Pageable var1);
}
