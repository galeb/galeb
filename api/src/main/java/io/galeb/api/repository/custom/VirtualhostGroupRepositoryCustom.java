package io.galeb.api.repository.custom;


import io.galeb.core.entity.VirtualhostGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("unused")
public interface VirtualhostGroupRepositoryCustom {

    VirtualhostGroup findOne(Long var1);

    Iterable<VirtualhostGroup> findAll(Sort var1);

    Page<VirtualhostGroup> findAll(Pageable var1);
}
