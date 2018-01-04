package io.galeb.api.repository;

import io.galeb.api.repository.custom.VirtualHostRepositoryCustom;
import io.galeb.core.entity.VirtualHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "virtualhost", collectionResourceRel = "virtualhost", itemResourceRel = "virtualhost")
public interface VirtualHostRepository extends JpaRepository<VirtualHost, Long>, VirtualHostRepositoryCustom {

    @Override
    @PreAuthorize("@authz.checkSave(principal, #virtualhost, #this)")
    VirtualHost save(@Param("virtualhost") VirtualHost virtualhost);

}