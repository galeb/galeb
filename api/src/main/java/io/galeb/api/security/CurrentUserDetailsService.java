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

import io.galeb.api.services.AccountDaoService;
import io.galeb.core.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LogManager.getLogger(CurrentUserDetailsService.class);

    @Autowired
    private AccountDaoService accountDaoService;

    @Autowired
    private LocalAdmin localAdmin;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (LocalAdmin.NAME.equals(username)) return localAdmin;
        Account account = accountDaoService.find(username);
        if (account == null) {
            throw new UsernameNotFoundException("Account " + username + " NOT FOUND");
        }
        return account;
    }

}
