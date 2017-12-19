package io.galeb.api.repository;

import io.galeb.api.repository.custom.VirtualHostRepositoryCustom;
import io.galeb.core.entity.VirtualHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "virtualhost", collectionResourceRel = "virtualhost")
public interface VirtualHostRepository extends JpaRepository<VirtualHost, Long>, VirtualHostRepositoryCustom {

}
