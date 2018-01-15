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

package io.galeb.oldapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.oldapi.v1entities.BalancePolicyType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class BalancePolicyTypeService {

    private static final Logger LOGGER = LogManager.getLogger(BalancePolicyTypeService.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public ResponseEntity<String> get() {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), "NULL");
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(Collections.singleton(emptyMap)));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> getWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), param);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }
    
    public ResponseEntity<String> post(String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> postWithParam(String param, String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase() + "/" + param, body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> put(String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> putWithParam(String param, String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase() + "/" + param, body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> delete() {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), "NULL");
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> deleteWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), param);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> patch(String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> patchWithParam(String param, String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase() + "/" + param, body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> options() {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), "NULL");
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> optionsWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), param);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> head() {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), "NULL");
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> headWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), param);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> trace() {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), "NULL");
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> traceWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(BalancePolicyType.class.getSimpleName().toLowerCase(), param);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }
}
