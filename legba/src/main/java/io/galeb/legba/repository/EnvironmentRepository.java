package io.galeb.legba.repository;

import io.galeb.core.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    Environment findByNameIgnoreCase(@Param("envName") String envName);
}
