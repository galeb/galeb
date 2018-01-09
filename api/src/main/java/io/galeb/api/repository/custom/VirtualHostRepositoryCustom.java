package io.galeb.api.repository.custom;


import io.galeb.core.entity.VirtualHost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("unused")
public interface VirtualHostRepositoryCustom extends WithRoles {

    VirtualHost findOne(Long var1);

    Iterable<VirtualHost> findAll(Sort var1);

    Page<VirtualHost> findAll(Pageable var1);

    void delete(Long id);
}
