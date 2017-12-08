package io.galeb.kratos.repository;

import io.galeb.core.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

}
