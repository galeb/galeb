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

package io.galeb.health.services;

import io.galeb.core.enums.SystemEnv;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xnio.Options;

import javax.annotation.PostConstruct;

@Service
public class SimpleWebServerService {

    private final int port = Integer.parseInt(SystemEnv.HEALTH_PORT.getValue());

    @Value("${build.project}")
    private String buildProject;

    @Value("${build.version}")
    private String buildVersion;

    @Value("${build.timestamp}")
    private String buildTimestamp;

    @PostConstruct
    public void init() {
        Undertow.builder().addHttpListener(port, "0.0.0.0", pingHandler())
                .setSocketOption(Options.KEEP_ALIVE, true)
                .setSocketOption(Options.REUSE_ADDRESSES, true)
                .setSocketOption(Options.TCP_NODELAY, true)
                .build().start();
    }

    private HttpHandler pingHandler() {

        String body = String.format("{\"name\":\"%s\", \"version\":\"%s\", \"build\":\"%s\", \"healthy\":\"WORKING\"}", buildProject, buildVersion, buildTimestamp);

        HttpHandler pingHandler = Handlers.path().addExactPath("/info", exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
            exchange.getResponseSender().send(body);
            exchange.endExchange();
        }).addPrefixPath("/", exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
            exchange.getResponseSender().send("WORKING");
            exchange.endExchange();
        });

        return pingHandler;
    }
}
