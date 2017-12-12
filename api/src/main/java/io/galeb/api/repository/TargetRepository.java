package io.galeb.api.repository;

import io.galeb.core.entity.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "target", collectionResourceRel = "target")
public interface TargetRepository extends JpaRepository<Target, Long> {
}
