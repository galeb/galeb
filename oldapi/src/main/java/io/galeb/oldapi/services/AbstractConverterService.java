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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import io.galeb.oldapi.entities.v1.Environment;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;

public abstract class AbstractConverterService<T extends AbstractEntity> {

    private final ObjectMapper mapper = new ObjectMapper();

    AbstractConverterService() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected io.galeb.core.entity.AbstractEntity mapToV2AbstractEntity(LinkedHashMap resource, Class<? extends io.galeb.core.entity.AbstractEntity> klazz) throws IOException {
        return mapper.readValue(mapper.writeValueAsString(resource), klazz);
    }

    protected String getEmptyMap() throws JsonProcessingException {
        return getEmptyMap(null);
    }

    protected String getEmptyMap(String body) throws JsonProcessingException {
        return getEmptyMap(null, body);
    }

    protected String getEmptyMap(Long id, String body) throws JsonProcessingException {
        Map<String, Object> emptyMap = new HashMap<>();
        body = body != null ? body : "NULL";
        emptyMap.put(Environment.class.getSimpleName().toLowerCase() + (id != null ? "/" + id : ""), body);
        return mapper.writeValueAsString(emptyMap);
    }

    protected abstract Set<Resource<T>> convertResources(ArrayList<LinkedHashMap> v2s);

    protected abstract T convertResource(LinkedHashMap resource) throws IOException;

    protected abstract String getResourceName();

    protected abstract ResponseEntity<PagedResources<Resource<T>>> getSearch(String findType, Map<String, String> queryMap);

    protected abstract ResponseEntity<PagedResources<Resource<T>>> get(Integer size, Integer page);

    protected abstract ResponseEntity<Resource<T>> getWithId(String id);

    protected abstract ResponseEntity<String> post(String body);

    protected abstract ResponseEntity<String> postWithId(String id, String body);

    protected abstract ResponseEntity<String> put(String body);

    protected abstract ResponseEntity<String> putWithId(String id, String body);

    protected abstract ResponseEntity<String> delete();

    protected abstract ResponseEntity<String> deleteWithId(String id);

    protected abstract ResponseEntity<String> patch(String body);

    protected abstract ResponseEntity<String> patchWithId(String id, String body);

    protected abstract ResponseEntity<String> options();

    protected abstract ResponseEntity<String> optionsWithId(String id);

    protected abstract ResponseEntity<String> head();

    protected abstract ResponseEntity<String> headWithId(String id);

    protected abstract ResponseEntity<String> trace();

    protected abstract ResponseEntity<String> traceWithId(String id);

    // TODO: Set Environment Status
    AbstractEntity.EntityStatus extractStatus() {
        return AbstractEntity.EntityStatus.UNKNOWN;
    }

}
