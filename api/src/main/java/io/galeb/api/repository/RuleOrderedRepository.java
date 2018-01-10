package io.galeb.api.repository;

import io.galeb.api.repository.custom.RuleOrderedRepositoryCustom;
import io.galeb.core.entity.RuleOrdered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "ruleordered", collectionResourceRel = "ruleordered", itemResourceRel = "ruleordered")
public interface RuleOrderedRepository extends JpaRepository<RuleOrdered, Long>, RuleOrderedRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #ruleordered, #this)")
    RuleOrdered save(@Param("ruleordered") RuleOrdered ruleordered);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, #id, #this)")
    RuleOrdered findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, principal, #this)")
    @Query("SELECT ro FROM RuleOrdered ro INNER JOIN ro.rule.project.teams t INNER JOIN t.accounts a WHERE a.username LIKE ?#{principal.username == @localAdmin.username ? '%' : principal.username}")
    Page<RuleOrdered> findAll(Pageable pageable);
}
