package io.galeb.api.repository;

import io.galeb.api.repository.custom.BalancePolicyRepositoryCustom;
import io.galeb.core.entity.BalancePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "balancepolicy", collectionResourceRel = "balancepolicy", itemResourceRel = "balancepolicy")
public interface BalancePolicyRepository extends JpaRepository<BalancePolicy, Long>, BalancePolicyRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #balancepolicy, #this)")
    BalancePolicy save(@Param("balancepolicy") BalancePolicy balancepolicy);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    Page<BalancePolicy> findByName(@Param("name") String name, Pageable pageable);

}
