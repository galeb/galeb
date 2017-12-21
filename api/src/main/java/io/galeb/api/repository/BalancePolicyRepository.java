package io.galeb.api.repository;

import io.galeb.core.entity.BalancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "balancepolicy", collectionResourceRel = "balancepolicy", itemResourceRel = "balancepolicy")
public interface BalancePolicyRepository extends JpaRepository<BalancePolicy, Long> {

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or principal.username == 'admin'")
    BalancePolicy save(BalancePolicy balancePolicy);

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or principal.username == 'admin'")
    void delete(Long id);

}
