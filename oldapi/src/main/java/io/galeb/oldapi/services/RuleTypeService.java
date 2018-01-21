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

import io.galeb.oldapi.entities.v1.RuleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RuleTypeService extends AbstractConverterService<RuleType> {

    private static final Logger LOGGER = LogManager.getLogger(RuleTypeService.class);

    private final String resourceName = RuleType.class.getSimpleName().toLowerCase();

    private final List<Link> links = Collections.singletonList(new Link("/" + resourceName + "/1", "self"));

    private final RuleType ruleTypeUrlPath = new RuleType("UrlPath");

    private final Resource<RuleType> resource = new Resource<>(ruleTypeUrlPath, links);

    @Override
    protected Set<Resource<RuleType>> convertResources(ArrayList<LinkedHashMap> v2s) {
        return null;
    }

    @Override
    protected RuleType convertResource(LinkedHashMap resource) throws IOException {
        return null;
    }

    @Override
    protected String getResourceName() {
        return resourceName;
    }

    @Override
    public ResponseEntity<PagedResources<Resource<RuleType>>> getSearch(String findType, Map<String, String> queryMap) {
        if ("findByName".equals(findType) && !"UrlPath".equals(queryMap.get("name"))) return ResponseEntity.notFound().build();
        if ("findByNameContaining".equals(findType) && !"UrlPath".equals(queryMap.get("name"))) return ResponseEntity.notFound().build();
        return get(0, 0);
    }

    @Override
    public ResponseEntity<PagedResources<Resource<RuleType>>> get(Integer size, Integer page) {
        Set<Resource<RuleType>> v1Resources = Collections.singleton(resource);
        final PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(1, 0, 1, 1);
        final PagedResources<Resource<RuleType>> pagedResources = new PagedResources<>(v1Resources, metadata, getBaseLinks());
        return ResponseEntity.ok(pagedResources);
    }

    public ResponseEntity<Resource<RuleType>> getWithId(String param) {
        return ResponseEntity.ok(resource);
    }
    
    public ResponseEntity<String> post(String body) {
        return ResponseEntity.created(URI.create("http://localhost")).build();
    }

    public ResponseEntity<String> postWithId(String param, String body) {
        return ResponseEntity.created(URI.create("http://localhost")).build();
    }

    public ResponseEntity<String> put(String body) {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<String> putWithId(String param, String body) {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<String> delete() {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<String> deleteWithId(String param) {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<String> patch(String body) {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<String> patchWithId(String param, String body) {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<String> options() {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<String> optionsWithId(String param) {
        return ResponseEntity.accepted().build();
    }

    public ResponseEntity<String> head() {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<String> headWithId(String param) {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<String> trace() {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<String> traceWithId(String param) {
        return ResponseEntity.noContent().build();
    }
}
