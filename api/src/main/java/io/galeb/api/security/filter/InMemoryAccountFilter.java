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

package io.galeb.api.security.filter;

import io.galeb.api.services.LocalAdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

public class InMemoryAccountFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LogManager.getLogger(InMemoryAccountFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String remoteUser = null;
        if (header != null && header.startsWith("Basic ")) {
            try {
                String[] tokens = extractAndDecodeHeader(header);
                remoteUser = tokens[0];
            } catch (Exception ignored) { }
        }
        if (remoteUser != null) {
            String remoteAddr = request.getRemoteAddr();
            if (LocalAdminService.NAME.equals(remoteUser) && (!"127.0.0.1".equals(remoteAddr) && !"0:0:0:0:0:0:0:1".equals(remoteAddr))) {
                throw new UsernameNotFoundException("Account " + LocalAdminService.NAME + " NOT FOUND");
            }
        }
        LOGGER.warn("InMemoryAccountFilter :" + remoteUser);
        filter.doFilter(request, response);
    }

    private String[] extractAndDecodeHeader(String header) throws IOException {
        byte[] base64Token = header.substring(6).getBytes("UTF-8");
        byte[] decoded;
        try {
            decoded = Base64.decode(base64Token);
        } catch (IllegalArgumentException ignored) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }

        String token = new String(decoded, Charset.forName("UTF-8"));
        int delim = token.indexOf(":");
        if (delim == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        } else {
            return new String[]{token.substring(0, delim), token.substring(delim + 1)};
        }
    }
}
