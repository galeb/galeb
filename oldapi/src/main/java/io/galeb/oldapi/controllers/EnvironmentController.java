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

package io.galeb.oldapi.controllers;

import io.galeb.oldapi.entities.v1.*;
import io.galeb.oldapi.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/environment")
public class EnvironmentController extends AbstractController<Environment> {

    private final EnvironmentService service;
    private final PoolService poolService;
    private final FarmService farmService;
    private final TargetService targetService;
    private final VirtualHostService virtualHostService;

    @Autowired
    public EnvironmentController(EnvironmentService service,
                                 PoolService poolService,
                                 FarmService farmService,
                                 TargetService targetService,
                                 VirtualHostService virtualHostService) {
        this.service = service;
        this.poolService = poolService;
        this.farmService = farmService;
        this.targetService = targetService;
        this.virtualHostService = virtualHostService;
    }

    @RequestMapping(value = "/search/{findType:findBy.+}",method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> getSearch(@PathVariable("findType") String findType,
                                                                                        @RequestParam Map<String, String> queryMap) {
        return service.getSearch(findType, queryMap);
    }

    @RequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> get(@RequestParam Map<String, String> queryMap) {
        return service.get(io.galeb.core.entity.Environment.class, queryMap);
    }

    @RequestMapping(value = "/{id:\\d+}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<? extends AbstractEntity>> getWithId(@PathVariable String id,
                                                                        @RequestParam Map<String, String> queryMap) {
        return service.getWithId(id, queryMap, io.galeb.core.entity.Environment.class);
    }

    @RequestMapping(method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<? extends AbstractEntity>> post(@RequestBody String body) {
        return service.post(body, io.galeb.core.entity.Environment.class);
    }

    @RequestMapping(value = "/{id:\\d+}", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postWithId(@PathVariable String id, @RequestBody String body) {
        return service.methodNotAllowed();
    }

    @RequestMapping(method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> put(@RequestBody String body) {
        return service.methodNotAllowed();
    }

    @RequestMapping(value = "/{id:\\d+}", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<? extends AbstractEntity>> putWithId(@PathVariable String id, @RequestBody String body) {
        return service.putWithId(id, body, io.galeb.core.entity.Environment.class);
    }

    @RequestMapping(method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delete() {
        return service.methodNotAllowed();
    }

    @RequestMapping(value = "/{id:\\d+}", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteWithId(@PathVariable String id) {
        return service.deleteWithId(id);
    }

    @RequestMapping(method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> patch(@RequestBody String body) {
        return service.methodNotAllowed();
    }

    @RequestMapping(value = "/{id:\\d+}", method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> patchWithId(@PathVariable String id, @RequestBody String body) {
        return service.patchWithId(id, body);
    }

    @RequestMapping(method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> options() {
        return service.methodNotAllowed();
    }

    @RequestMapping(value = "/{id:\\d+}", method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> optionsWithId(@PathVariable String id) {
        return service.methodNotAllowed();
    }

    @RequestMapping(method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> head() {
        return service.head();
    }

    @RequestMapping(value = "/{id:\\d+}", method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> headWithId(@PathVariable String id) {
        return service.methodNotAllowed();
    }

    @RequestMapping(method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> trace() {
        return service.methodNotAllowed();
    }

    @RequestMapping(value = "/{id:\\d+}", method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> traceWithId(@PathVariable String id) {
        return service.methodNotAllowed();
    }

    // MAPPING TO MANY: pools, farms, targets & virtualhosts

    @RequestMapping(value = "/{id:\\d+}/pools", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> getMapping(@PathVariable String id) {
        return poolService.getSearch("findByEnviromentId", queryWithIdOnly(id));
    }

    @RequestMapping(value = "/{id:\\d+}/farms", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> getFarms(@PathVariable String id) {
        return farmService.getSearch("findByEnviromentId", queryWithIdOnly(id));
    }

    @RequestMapping(value = "/{id:\\d+}/targets", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> getTargets(@PathVariable String id) {
        return targetService.getSearch("findByEnviromentId", queryWithIdOnly(id));
    }

    @RequestMapping(value = "/{id:\\d+}/virtualhosts", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> getVirtualhosts(@PathVariable String id) {
        return virtualHostService.getSearch("findByEnviromentId", queryWithIdOnly(id));
    }

    // MAPPING TO ONE - NOT EXIST :)

}
