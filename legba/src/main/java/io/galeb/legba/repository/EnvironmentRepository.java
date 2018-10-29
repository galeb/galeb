package io.galeb.legba.repository;

import io.galeb.core.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    @Query(value = "SELECT e.id FROM environment e WHERE LOWER(e.name) = LOWER(:envName)", nativeQuery = true)
    Long idFromName(@Param("envName") String envName);
}
