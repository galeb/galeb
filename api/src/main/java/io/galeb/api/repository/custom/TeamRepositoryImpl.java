package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.RoleGroup;
import io.galeb.core.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TeamRepositoryImpl extends AbstractRepositoryImplementation<Team> implements TeamRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Team.class, em);
        setStatusService(statusService);
    }

    @Override
    public Set<String> roles(Object principal, Object criteria) {
        Account account = (Account) principal;
        List<RoleGroup> roleGroups = em.createNamedQuery("roleGroupsTeam", RoleGroup.class)
                .setParameter("team_id", ((Team)criteria).getId())
                .setParameter("account_id", account.getId())
                .getResultList();
        return roleGroups.stream().flatMap(rg -> rg.getRoles().stream()).map(Enum::toString).collect(Collectors.toSet());
    }

}
