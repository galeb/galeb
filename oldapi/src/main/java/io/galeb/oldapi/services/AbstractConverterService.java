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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractConverterService<T extends AbstractEntity> {

    protected final ObjectMapper mapper = new ObjectMapper();

    AbstractConverterService() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

    @SuppressWarnings("unchecked")
    ArrayList<LinkedHashMap> jsonToList(String body) throws IOException {
        return (ArrayList<LinkedHashMap>) ((LinkedHashMap)
                mapper.readValue(body, HashMap.class).get("_embedded")).get(getResourceName());
    }

    // TODO: Set Environment Status
    AbstractEntity.EntityStatus extractStatus() {
        return AbstractEntity.EntityStatus.UNKNOWN;
    }

}
