package io.galeb.api.repository.custom;

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.RoleGroup;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class AccountRepositoryImpl extends AbstractRepositoryImplementation<Account> implements AccountRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Account.class, em);
        setStatusService(statusService);
    }

    @Override
    public Set<String> roles(Object principal, Object criteria) {
        Account account = ((Account) principal);
        Set<String> roles = account.getRolegroups().stream().flatMap(rg -> rg.getRoles().stream())
                .map(Object::toString).distinct().collect(Collectors.toSet());
        List<RoleGroup> roleGroups = em.createNamedQuery("roleGroupsFromTeams", RoleGroup.class)
                .setParameter("id", account.getId())
                .getResultList();
        roles.addAll(roleGroups.stream().flatMap(rg -> rg.getRoles().stream())
                .map(Object::toString).distinct().collect(Collectors.toSet()));
        return roles;
    }

}
