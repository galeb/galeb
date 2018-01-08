package io.galeb.api.repository;

import io.galeb.core.entity.BalancePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "balancepolicy", collectionResourceRel = "balancepolicy", itemResourceRel = "balancepolicy")
public interface BalancePolicyRepository extends JpaRepository<BalancePolicy, Long> {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #balancePolicy, #this)")
    BalancePolicy save(@Param("balancePolicy") BalancePolicy balancePolicy);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    Page<BalancePolicy> findByName(@Param("name") String name, Pageable pageable);

}
