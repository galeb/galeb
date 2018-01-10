package io.galeb.api.repository;

import io.galeb.api.repository.custom.HealthCheckRepositoryCustom;
import io.galeb.core.entity.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "healthcheck", collectionResourceRel = "healthcheck", itemResourceRel = "healthcheck")
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long>, HealthCheckRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #healthcheck, #this)")
    HealthCheck save(@Param("healthcheck") HealthCheck healthcheck);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, #id, #this)")
    HealthCheck findOne(@Param("id") Long id);

}
