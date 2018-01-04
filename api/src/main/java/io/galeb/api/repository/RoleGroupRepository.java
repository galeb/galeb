package io.galeb.api.repository;

import io.galeb.core.entity.RoleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "rolegroup", collectionResourceRel = "rolegroup", itemResourceRel = "rolegroup")
public interface RoleGroupRepository extends JpaRepository<RoleGroup, Long> {
}
