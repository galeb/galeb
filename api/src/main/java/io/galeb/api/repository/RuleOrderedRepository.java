package io.galeb.api.repository;

import io.galeb.core.entity.RuleOrdered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "ruleordered", collectionResourceRel = "ruleordered")
public interface RuleOrderedRepository extends JpaRepository<RuleOrdered, Long> {
}
