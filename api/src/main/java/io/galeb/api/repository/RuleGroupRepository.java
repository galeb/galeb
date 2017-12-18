package io.galeb.api.repository;

import io.galeb.core.entity.RuleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "rulegroup", collectionResourceRel = "rulegroup")
public interface RuleGroupRepository extends JpaRepository<RuleGroup, Long> {
}
