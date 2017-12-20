package io.galeb.api.repository;

import io.galeb.core.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "project", collectionResourceRel = "project", itemResourceRel = "project")
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
