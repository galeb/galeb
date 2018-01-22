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

package io.galeb.oldapi.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.core.entity.Account;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/token")
@SuppressWarnings("unused")
public class TokenController {

    private static final Log LOGGER = LogFactory.getLog(TokenController.class);
    private static final Pageable ALL_PAGE = new PageRequest(0, Integer.MAX_VALUE);

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isFullyAuthenticated()")
    public String token(HttpSession session) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("token", session.getId());
        String loginName = currentUser.getName();
        tokenInfo.put("account", loginName);
        Account account = (Account) currentUser.getPrincipal();
        tokenInfo.put("email", account.getEmail());
        tokenInfo.put("hasTeam", false);
        tokenInfo.put("admin", false);
        String json = "{}";
        try {
            json = mapper.writeValueAsString(tokenInfo);
        } catch (JsonProcessingException e) {
            LOGGER.error(e);
        }

        return json;
    }

}
