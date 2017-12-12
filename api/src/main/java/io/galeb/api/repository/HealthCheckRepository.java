package io.galeb.api.repository;

import io.galeb.core.entity.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "healthcheck", collectionResourceRel = "healthcheck")
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {
}
