package io.galeb.api.repository;

import io.galeb.core.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "environment", collectionResourceRel = "environment")
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
}
