package io.galeb.api.repository;

import io.galeb.core.entity.HealthStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "healthstatus", collectionResourceRel = "healthstatus", itemResourceRel = "healthstatus")
public interface HealthStatusRepository extends JpaRepository<HealthStatus, Long> {
}
