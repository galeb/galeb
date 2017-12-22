/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.api.security;

import io.galeb.core.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.UUID;

import static com.google.common.hash.Hashing.sha256;

@Service
public class CurrentUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LogManager.getLogger(CurrentUserDetailsService.class);

    private static final Account LOCAL_ADMIN;
    static {
        String apitoken = sha256().hashBytes(UUID.randomUUID().toString().getBytes()).toString();
        LOCAL_ADMIN = new Account();
        LOCAL_ADMIN.setUsername("admin");
        LOCAL_ADMIN.setPassword(apitoken);
        LOCAL_ADMIN.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        LOGGER.info(">>> Local Token: " + apitoken);
    }

    @PersistenceContext
    private EntityManager em;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if ("admin".equals(username)) return localAdmin();
        Account account = null;
        try {
            account = (Account) em.createQuery("SELECT a FROM Account a WHERE a.username = :username").setParameter("username", username).getSingleResult();
        } catch (NoResultException ignore) { }
        if (account == null) {
            throw new UsernameNotFoundException("Account " + username + " NOT FOUND");
        }
        return account;
    }

    public static Account localAdmin() {
        return LOCAL_ADMIN;
    }

}
