package io.galeb.api.repository;

import io.galeb.core.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "rule", collectionResourceRel = "rule", itemResourceRel = "rule")
public interface RuleRepository extends JpaRepository<Rule, Long> {
}
