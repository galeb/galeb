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

package io.galeb.router.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

@Component
public class InfoHandler implements HttpHandler {

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.putAttachment(PoolHandler.POOL_NAME, "__info__");
        exchange.putAttachment(PathGlobHandler.RULE_NAME, "__info__");

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
        exchange.setStatusCode(StatusCodes.OK);
        long uptimeJVM = ManagementFactory.getRuntimeMXBean().getUptime();
        String uptime = getUptimeSO();
        String version = getClass().getPackage().getImplementationVersion();
        Map<String, Object> infoJson = new HashMap<>();
        infoJson.put("uptime-so", uptime);
        infoJson.put("uptime-jvm", uptimeJVM);
        infoJson.put("version", version);
        exchange.getResponseSender().send(gson.toJson(infoJson));
        exchange.endExchange();
    }

    private String getUptimeSO() {
        ProcessBuilder processBuilder = new ProcessBuilder("uptime");
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            return IOUtils.toString(process.getInputStream(), "UTF-8").replace("\n", "");
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return "";
        }
    }

}
