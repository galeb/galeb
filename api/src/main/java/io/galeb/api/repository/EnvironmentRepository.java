package io.galeb.api.repository;

import io.galeb.core.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Set;

@RepositoryRestResource(path = "environment", collectionResourceRel = "environment")
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

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

}
