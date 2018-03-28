package io.galeb.legba.repository;

import io.galeb.core.entity.VirtualHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(exported = false, path = "virtualhost", collectionResourceRel = "virtualhost", itemResourceRel = "virtualhost")
public interface VirtualHostRepository extends JpaRepository<VirtualHost, Long> {

    @RestResource(exported = false)
    @Query(value = "SELECT DISTINCT v FROM VirtualHost as v " +
            "inner join v.environments as e " +
            "WHERE e.id = :envId")
    List<VirtualHost> findAllByEnvironmentId(@Param("envId") Long envId);
}