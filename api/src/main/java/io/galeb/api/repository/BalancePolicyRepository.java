package io.galeb.api.repository;

import io.galeb.core.entity.BalancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "balancepolicy", collectionResourceRel = "balancepolicy")
public interface BalancePolicyRepository extends JpaRepository<BalancePolicy, Long> {
}
