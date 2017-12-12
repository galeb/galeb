package io.galeb.api.repository;

import io.galeb.core.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "team", collectionResourceRel = "team")
public interface TeamRepository extends JpaRepository<Team, Long> {
}
