package io.galeb.api.repository;

import io.galeb.core.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "account", collectionResourceRel = "account")
public interface AccountRepository extends JpaRepository<Account, Long> {
}
