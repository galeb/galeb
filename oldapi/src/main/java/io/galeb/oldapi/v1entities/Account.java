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

package io.galeb.oldapi.v1entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

public class Account extends AbstractEntity<Account> {

    private static final long serialVersionUID = -2745836665462717899L;

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }

    private final Set<Team> teams = new HashSet<>();

    private String password;

    private String email;

    private final Set<Role> roles = new HashSet<>();

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        Assert.hasText(email, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        this.email = email;
        return this;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public Account setTeams(Set<Team> teams) {
        if (teams != null) {
            this.teams.clear();
            this.teams.addAll(teams);
        }
        return this;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Account setRoles(Set<Role> roles) {
        if (roles != null) {
            this.roles.clear();
            this.roles.addAll(roles);
        }
        return this;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty(value = "password", required = true)
    public Account setPassword(String password) {
        Assert.hasText(password, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        this.password = ENCODER.encode(password);
        return this;
    }
}
