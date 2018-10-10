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

package io.galeb.api.services;

import io.galeb.core.entity.Account;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Service
@Transactional
public class AccountDaoService {

    @PersistenceContext
    private EntityManager entityManager;

    public Account save(Account account) {
        entityManager.persist(account);
        return find(account.getUsername());
    }

    @Cacheable(value = "userDetails")
    public Account find(String username) {
        Account accountPersisted = null;
        try {
            accountPersisted = entityManager.createQuery("SELECT a FROM Account a WHERE a.username = :username", Account.class)
                    .setParameter("username", username).getSingleResult();
        } catch (NoResultException ignored) {}
        return accountPersisted;
    }

}
