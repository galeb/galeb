package io.galeb.api.repository;

import io.galeb.api.repository.custom.TeamRepositoryCustom;
import io.galeb.core.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "team", collectionResourceRel = "team", itemResourceRel = "team")
public interface TeamRepository extends JpaRepository<Team, Long>, TeamRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #team, #this)")
    Team save(@Param("team") Team team);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, #id, #this)")
    Team findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, principal, #this)")
    @Query("SELECT t FROM Team t INNER JOIN t.accounts a WHERE a.username LIKE ?#{principal.username == @localAdmin.username ? '%' : principal.username}")
    Page<Team> findAll(Pageable pageable);

}
