package io.galeb.api.repository;

import io.galeb.api.repository.custom.TargetRepositoryCustom;
import io.galeb.core.entity.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "target", collectionResourceRel = "target", itemResourceRel = "target")
public interface TargetRepository extends JpaRepository<Target, Long>, TargetRepositoryCustom {
}
