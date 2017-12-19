package io.galeb.api.repository;

import io.galeb.core.entity.VirtualhostGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "virtualhostgroup", collectionResourceRel = "virtualhostgroup")
public interface VirtualhostGroupRepository extends JpaRepository<VirtualhostGroup, Long> {

    @Override
    @RestResource(exported = false)
    @SuppressWarnings("unchecked")
    VirtualhostGroup save(VirtualhostGroup virtualhostgroup);
}
