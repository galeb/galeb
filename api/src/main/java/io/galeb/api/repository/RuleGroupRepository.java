package io.galeb.api.repository;

import io.galeb.core.entity.RuleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "rulegroup", collectionResourceRel = "rulegroup")
public interface RuleGroupRepository extends JpaRepository<RuleGroup, Long> {

    @Override
    @RestResource(exported = false)
    @SuppressWarnings("unchecked")
    RuleGroup save(RuleGroup ruleGroup);
}
