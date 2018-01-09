package io.galeb.api.repository;

import io.galeb.api.repository.custom.TargetRepositoryCustom;
import io.galeb.core.entity.Target;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "target", collectionResourceRel = "target", itemResourceRel = "target")
public interface TargetRepository extends JpaRepository<Target, Long>, TargetRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #target, #this)")
    Target save(@Param("target") Target target);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, #id, #this)")
    Target findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, principal, #this)")
    @Query("SELECT t FROM Target t INNER JOIN t.pools pools INNER JOIN pools.project p INNER JOIN p.teams t INNER JOIN t.accounts a WHERE a.username LIKE ?#{principal.username == @localAdmin.username ? '%' : principal.username}")
    Page<Target> findAll(Pageable pageable);
}
