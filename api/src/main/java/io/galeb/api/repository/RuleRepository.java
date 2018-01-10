package io.galeb.api.repository;

import io.galeb.api.repository.custom.RuleRepositoryCustom;
import io.galeb.core.entity.Rule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "rule", collectionResourceRel = "rule", itemResourceRel = "rule")
public interface RuleRepository extends JpaRepository<Rule, Long>, RuleRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #rule, #this)")
    Rule save(@Param("rule") Rule rule);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, #id, #this)")
    Rule findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, principal, #this)")
    @Query("SELECT r FROM Rule r LEFT JOIN r.project.teams t INNER JOIN t.accounts a " +
           "WHERE a.username LIKE ?#{principal.username == @localAdmin.username ? '%' : principal.username} OR r.global = true")
    Page<Rule> findAll(Pageable pageable);
}
