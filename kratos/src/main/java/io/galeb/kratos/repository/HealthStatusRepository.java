package io.galeb.kratos.repository;

import io.galeb.core.entity.HealthStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface HealthStatusRepository extends JpaRepository<HealthStatus, Long> {

}
