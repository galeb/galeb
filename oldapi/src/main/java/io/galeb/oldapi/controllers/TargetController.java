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

import io.galeb.oldapi.entities.v1.Target;
import io.galeb.oldapi.services.TargetService;
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
@RequestMapping("/target")
public class TargetController extends AbstractController<Target> {

    @Autowired
    private TargetService service;

    @RequestMapping(value = "/search/{findType:findBy.+}",method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<Target>>> getSearch(@PathVariable("findType") String findType,
                                                                      @RequestParam Map<String, String> queryMap) {
        return service.getSearch(findType, queryMap);
    }

    @RequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<Target>>> get(@RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "page", required = false) Integer page) {
        return service.get(size, page);
    }

    @RequestMapping(value = "/{id:\\d+}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Target>> getWithId(@PathVariable String id) {
        return service.getWithId(id);
    }

    @RequestMapping(method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> post(@RequestBody String body) {
        return service.post(body);
    }

    @RequestMapping(value = "/{id:\\d+}", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postWithId(@PathVariable String id, @RequestBody String body) {
        return service.postWithId(id, body);
    }

    @RequestMapping(method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> put(@RequestBody String body) {
        return service.put(body);
    }

    @RequestMapping(value = "/{id:\\d+}", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> putWithId(@PathVariable String id, @RequestBody String body) {
        return service.putWithId(id, body);
    }

    @RequestMapping(method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delete() {
        return service.delete();
    }

    @RequestMapping(value = "/{id:\\d+}", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteWithId(@PathVariable String id) {
        return service.deleteWithId(id);
    }

    @RequestMapping(method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> patch(@RequestBody String body) {
        return service.patch(body);
    }

    @RequestMapping(value = "/{id:\\d+}", method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> patchWithId(@PathVariable String id, @RequestBody String body) {
        return service.patchWithId(id, body);
    }

    @RequestMapping(method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> options() {
        return service.options();
    }

    @RequestMapping(value = "/{id:\\d+}", method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> optionsWithId(@PathVariable String id) {
        return service.optionsWithId(id);
    }

    @RequestMapping(method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> head() {
        return service.head();
    }

    @RequestMapping(value = "/{id:\\d+}", method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> headWithId(@PathVariable String id) {
        return service.headWithId(id);
    }

    @RequestMapping(method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> trace() {
        return service.trace();
    }

    @RequestMapping(value = "/{id:\\d+}", method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> traceWithId(@PathVariable String id) {
        return service.traceWithId(id);
    }

}
