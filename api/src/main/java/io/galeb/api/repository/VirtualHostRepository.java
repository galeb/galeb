package io.galeb.api.repository;

import io.galeb.api.repository.custom.VirtualHostRepositoryCustom;
import io.galeb.core.entity.VirtualHost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "virtualhost", collectionResourceRel = "virtualhost", itemResourceRel = "virtualhost")
public interface VirtualHostRepository extends JpaRepository<VirtualHost, Long>, VirtualHostRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #virtualhost, #this)")
    VirtualHost save(@Param("virtualhost") VirtualHost virtualhost);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, #id, #this)")
    VirtualHost findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, principal, #this)")
    @Query("SELECT v FROM VirtualHost v INNER JOIN v.project.teams t INNER JOIN t.accounts a WHERE a.username LIKE ?#{principal.username == @localAdmin.username ? '%' : principal.username}")
    Page<VirtualHost> findAll(Pageable pageable);

}