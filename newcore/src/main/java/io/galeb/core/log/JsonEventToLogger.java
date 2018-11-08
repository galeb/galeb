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

package io.galeb.core.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.galeb.core.enums.SystemEnv;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class JsonEventToLogger extends ObjectNode {

    // @formatter:off
    private static final ObjectMapper       MAPPER       = new ObjectMapper();
    private static final Map<Class, Logger> LOGGERS      = new ConcurrentHashMap<>();
    private static final String             LOGGING_TAGS = SystemEnv.LOGGING_TAGS.getValue();
    // @formatter:on

    private final Logger logger;

    public JsonEventToLogger(final Class klazz) {
        this(LOGGERS.computeIfAbsent(klazz, v -> LogManager.getLogger(klazz)));
        put("class", klazz.getSimpleName());
        put("hostname", System.getenv("HOSTNAME"));
        put("tags", LOGGING_TAGS);
        put("timestamp", System.currentTimeMillis());
    }

    private JsonEventToLogger(Logger logger) {
        this(JsonNodeFactory.instance, logger);
    }

    private JsonEventToLogger(JsonNodeFactory nc, Logger logger) {
        this(nc, new LinkedHashMap<>(), logger);
    }

    private JsonEventToLogger(JsonNodeFactory nc, Map<String, JsonNode> kids, Logger logger) {
        super(nc, kids);
        this.logger = logger;
    }

    public void sendDebug() {
        if (logger.isDebugEnabled()) {
            logger.debug(toString());
        }
    }

    public void sendDebug(Throwable throwable) {
        if (logger.isDebugEnabled()) {
            put("throwableMessage", throwable.getMessage());
            try {
                set("throwable_stack", MAPPER.convertValue(throwable, JsonNode.class));
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
            }
            sendDebug();
        }
    }

    public void sendInfo() {
        logger.info(toString());
    }

    public void sendInfo(Throwable throwable) {
        put("throwableMessage", throwable.getMessage());
        try {
            set("throwable_stack", MAPPER.convertValue(throwable, JsonNode.class));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
        sendInfo();
    }

    public void sendWarn() {
        logger.warn(toString());
    }

    public void sendWarn(Throwable throwable) {
        put("throwableMessage", throwable.getMessage());
        try {
            set("throwable_stack", MAPPER.convertValue(throwable, JsonNode.class));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
        sendWarn();
    }

    public void sendError() {
        logger.error(toString());
    }

    public void sendError(Throwable throwable) {
        put("throwableMessage", throwable.getMessage());
        try {
            set("throwable_stack", MAPPER.convertValue(throwable, JsonNode.class));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
        sendError();
    }

}
