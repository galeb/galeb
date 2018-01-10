package io.galeb.api.repository;

import io.galeb.api.repository.custom.EnvironmentRepositoryCustom;
import io.galeb.core.entity.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Set;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "environment", collectionResourceRel = "environment", itemResourceRel = "environment")
public interface EnvironmentRepository extends JpaRepository<Environment, Long>, EnvironmentRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #environment, #this)")
    Environment save(@Param("environment") Environment environment);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @RestResource(exported = false)
    @Query(value = "SELECT DISTINCT e FROM Environment as e " +
            "inner join e.pools as p " +
            "inner join p.targets as t " +
            "WHERE t.id = :targetId")
    Set<Environment> findAllByTargetId(@Param("targetId") long targetId);

    @RestResource(exported = false)
    @Query(value = "SELECT DISTINCT e FROM Environment as e " +
            "inner join e.virtualhosts as v " +
            "inner join v.virtualhostgroup as vp " +
            "inner join vp.rulesordered as ro " +
            "WHERE ro.id = :ruleorderedId")
    Set<Environment> findAllByRuleOrderedId(@Param("ruleorderedId") long ruleorderedId);

    @RestResource(exported = false)
    @Query(value = "SELECT DISTINCT e FROM Environment as e " +
            "inner join e.pools as p " +
            "inner join p.rules as r " +
            "WHERE r.id = :ruleId")
    Set<Environment> findAllByRuleId(@Param("ruleId") long ruleId);

    Page<Environment> findByName(@Param("name") String name, Pageable pageable);
}
