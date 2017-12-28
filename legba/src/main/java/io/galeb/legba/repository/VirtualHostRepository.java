package io.galeb.legba.repository;

import io.galeb.core.entity.VirtualHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "virtualhost", collectionResourceRel = "virtualhost", itemResourceRel = "virtualhost")
public interface VirtualHostRepository extends JpaRepository<VirtualHost, Long> {

}