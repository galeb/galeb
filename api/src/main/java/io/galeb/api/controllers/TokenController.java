/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.api.controllers;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.galeb.api.services.LocalAdminService;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.RoleGroup;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@RestController
public class TokenController {

    @GetMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<TokenInfo> token() {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final TokenInfo tokenInfo = new TokenInfo(account);
        return ResponseEntity.ok(tokenInfo);
    }

    @JsonPropertyOrder({"username", "email", "token", "roles", "admin"})
    public static class TokenInfo {
        private String username ;
        private String email;
        private String token;
        private SortedSet<String> roles;

        public TokenInfo(Account account) {
            this.username = account.getUsername();
            this.email = account.getEmail();
            this.token = account.getApitoken();
            this.roles = account.getRolegroups().stream()
                    .flatMap(r -> r.getRoles().stream())
                    .map(Enum::name)
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getToken() {
            return token;
        }

        public Set<String> getRoles() {
            return roles;
        }

        // TODO: Remove when web-ui will be improved
        @Deprecated
        public boolean isAdmin() {
            return roles.contains(RoleGroup.ROLEGROUP_SUPER_ADMIN) || LocalAdminService.NAME.equals(username);
        }
    }
}
