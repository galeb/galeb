package io.galeb.api.repository;

import io.galeb.api.repository.custom.VirtualhostGroupRepositoryCustom;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.entity.VirtualhostGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collection;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "virtualhostgroup", collectionResourceRel = "virtualhostgroup", itemResourceRel = "virtualhostgroup")
public interface VirtualhostGroupRepository extends JpaRepository<VirtualhostGroup, Long>, VirtualhostGroupRepositoryCustom {

    @Override
    @RestResource(exported = false)
    @PreAuthorize("@perm.allowSave(principal, #virtualhostgroup, #this)")
    VirtualhostGroup save(@Param("virtualhostgroup") VirtualhostGroup virtualhostgroup);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, #id, #this)")
    VirtualhostGroup findOne(@Param("id") Long id);

    VirtualhostGroup findByVirtualhostsIn(@Param("virtualhosts") Collection<VirtualHost> virtualhosts);
}
