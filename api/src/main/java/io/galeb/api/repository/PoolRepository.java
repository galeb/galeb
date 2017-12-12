package io.galeb.api.repository;

import io.galeb.core.entity.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "pool", collectionResourceRel = "pool")
public interface PoolRepository extends JpaRepository<Pool, Long> {
}
