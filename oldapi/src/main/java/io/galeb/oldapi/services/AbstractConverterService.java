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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractConverterService<T> {

    protected final ObjectMapper mapper = new ObjectMapper();

    AbstractConverterService() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SuppressWarnings("unchecked")
    private Link convertLink(Map.Entry<String, Object> entry) {
        String href = ((LinkedHashMap<String, String>) entry.getValue()).get("href")
                .replaceAll(".*/" + getResourceName(), "/" + getResourceName());
        return new Link(href, entry.getKey());
    }

    protected abstract Set<Resource<T>> convertResources(ArrayList<LinkedHashMap> v2s);

    protected abstract T convertResource(LinkedHashMap resource) throws IOException;

    protected abstract String getResourceName();

    @SuppressWarnings("unchecked")
    List<Link> extractLinks(LinkedHashMap resource) {
        return ((LinkedHashMap<String, Object>) resource.get("_links")).entrySet().stream()
                .map(this::convertLink)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    ArrayList<LinkedHashMap> jsonToList(String body) throws IOException {
        return (ArrayList<LinkedHashMap>) ((LinkedHashMap)
                mapper.readValue(body, HashMap.class).get("_embedded")).get(getResourceName());
    }

    // TODO: Set Environment Status
    AbstractEntity.EntityStatus extractStatus() {
        return AbstractEntity.EntityStatus.UNKNOWN;
    }

    List<Link> getBaseLinks() {
        final List<Link> links = new ArrayList<>();
        links.add(new Link("/" + getResourceName() + "?page=0&size=1000{&sort}", "self"));
        links.add(new Link("/" + getResourceName() + "/search", "search"));
        return links;
    }

    public abstract ResponseEntity<PagedResources<Resource<T>>> getSearch(String findType, Map<String, String> queryMap);
}
