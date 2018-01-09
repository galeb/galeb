package io.galeb.api.repository;

import io.galeb.api.repository.custom.ProjectRepositoryCustom;
import io.galeb.core.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "project", collectionResourceRel = "project", itemResourceRel = "project")
public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectRepositoryCustom {

    Page<Project> findByName(@Param("name") String name, Pageable pageable);
}
