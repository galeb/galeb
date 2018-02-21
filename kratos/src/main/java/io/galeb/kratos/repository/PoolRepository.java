package io.galeb.kratos.repository;

import java.util.Set;
import io.galeb.core.entity.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface PoolRepository extends JpaRepository<Pool, Long> {

    @Query("SELECT p FROM Pool p INNER JOIN p.targets t WHERE t.id = :targetId")
    Pool findByTargetId(@Param("targetId") long targetId);

}
