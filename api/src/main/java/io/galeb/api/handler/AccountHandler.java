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

package io.galeb.api.handler;

import static com.google.common.hash.Hashing.sha256;

import io.galeb.core.entity.Account;
import java.nio.charset.Charset;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AccountHandler extends AbstractHandler<Account> {

    @Override
    protected void onBeforeSave(Account account) {
        super.onBeforeSave(account);
        if (account.getResettoken() != null && account.getResettoken()) {
            account.setApitoken(sha256().hashString(UUID.randomUUID().toString(), Charset.defaultCharset()).toString());
            account.setResettoken(false);
        }
    }

}
