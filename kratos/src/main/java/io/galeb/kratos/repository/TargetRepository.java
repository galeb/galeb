package io.galeb.kratos.repository;

import io.galeb.core.entity.Target;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TargetRepository extends JpaRepository<Target, Long> {

    @Query("SELECT t FROM Target t INNER JOIN t.pools p WHERE p.environment = :env")
    Page<Target> findByEnvironmentName(String name, Pageable pageable);

}
