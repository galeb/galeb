package io.galeb.api.repository;

import io.galeb.api.repository.custom.PoolRepositoryCustom;
import io.galeb.core.entity.Pool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "pool", collectionResourceRel = "pool", itemResourceRel = "pool")
public interface PoolRepository extends JpaRepository<Pool, Long>, PoolRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #pool, #this)")
    Pool save(@Param("pool") Pool pool);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, #id, #this)")
    Pool findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, principal, #this)")
    @Query("SELECT pools FROM Pool pools LEFT JOIN pools.project p INNER JOIN p.teams t INNER JOIN t.accounts a LEFT JOIN pools.rules r " +
           "WHERE a.username LIKE ?#{principal.username == @localAdmin.username ? '%' : principal.username} OR r.global = true")
    Page<Pool> findAll(Pageable pageable);
}
