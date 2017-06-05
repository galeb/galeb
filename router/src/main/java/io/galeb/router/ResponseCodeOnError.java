/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

package io.galeb.router;

import io.undertow.server.HttpHandler;
import io.undertow.util.HttpString;

import static io.undertow.util.HttpString.tryFromString;

@SuppressWarnings("unused")
public enum ResponseCodeOnError {
    RULE_PATH_NOT_FOUND              ("RULE_PATH_NOT_FOUND",              503),
    PROXY_HANDLER_NOT_DEFINED        ("PROXY_HANDLER_NOT_DEFINED",        503),
    POOL_NOT_DEFINED                 ("POOL_NOT_DEFINED",                 503),
    ETCD_VIRTUALHOSTS_PATH_NOT_FOUND ("ETCD_VIRTUALHOSTS_PATH_NOT_FOUND", 503),
    VIRTUALHOST_NOT_FOUND            ("VIRTUALHOST_NOT_FOUND",            503),
    RULES_EMPTY                      ("RULES_EMPTY",                      503),
    HOSTS_EMPTY                      ("HOSTS_EMPTY",                      502),
    IPACL_FORBIDDEN                  ("FORBIDDEN",                        403),
    ROOT_HANDLER_FAILED              ("ROOT_HANDLER_FAILED",              503),
    COULD_NOT_RESOLVE_BACKEND        ("COULD_NOT_RESOLVE_BACKEND",        0), // HttpStatus NOT modifiable
    QUEUED_REQUEST_FAILED            ("QUEUED_REQUEST_FAILED",            0); // HttpStatus NOT modifiable

    public static class Header {
        public static final HttpString X_GALEB_ERROR = tryFromString("X-Galeb-Error");
    }

    private final String message;
    private final int statusCode;

    ResponseCodeOnError(final String message, final int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public HttpHandler getHandler() {
        return exchange -> {
            exchange.getResponseHeaders().put(Header.X_GALEB_ERROR, message);
            if (statusCode != 0) exchange.setStatusCode(statusCode);
            exchange.endExchange();
        };
    }

    public String getMessage() {
        return message;
    }
}
