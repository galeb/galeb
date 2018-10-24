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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class JsonLogger extends ObjectNode {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<Class, Logger> LOGGERS = new ConcurrentHashMap<>();

    private final Logger logger;

    public static JsonLogger instance(final Class klazz) {
        return new JsonLogger(LOGGERS.computeIfAbsent(klazz, v -> LogManager.getLogger(klazz)));
    }

    private JsonLogger(Logger logger) {
        this(JsonNodeFactory.instance, logger);
    }

    private JsonLogger(JsonNodeFactory nc, Logger logger) {
        this(nc, new LinkedHashMap<>(), logger);
    }

    private JsonLogger(JsonNodeFactory nc, Map<String, JsonNode> kids, Logger logger) {
        super(nc, kids);
        this.logger = logger;
    }

    public void debug() {
        logger.debug(toString());
    }

    public void debug(Throwable throwable) {
        put("throwableMessage", throwable.getMessage());
        try {
            set("throwable_stack", MAPPER.convertValue(throwable, JsonNode.class));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
        debug();
    }

    public void info() {
        logger.info(toString());
    }

    public void info(Throwable throwable) {
        put("throwableMessage", throwable.getMessage());
        try {
            set("throwable_stack", MAPPER.convertValue(throwable, JsonNode.class));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
        info();
    }

    public void warn() {
        logger.warn(toString());
    }

    public void warn(Throwable throwable) {
        put("throwableMessage", throwable.getMessage());
        try {
            set("throwable_stack", MAPPER.convertValue(throwable, JsonNode.class));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
        warn();
    }

    public void error() {
        logger.error(toString());
    }

    public void error(Throwable throwable) {
        put("throwableMessage", throwable.getMessage());
        try {
            set("throwable_stack", MAPPER.convertValue(throwable, JsonNode.class));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
        error();
    }

}
