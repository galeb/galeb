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

import io.galeb.oldapi.services.EnvironmentService;
import io.galeb.oldapi.entities.v1.Environment;
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

import static org.springframework.web.bind.annotation.RequestMethod.*;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/environment")
public class EnvironmentController {

    @Autowired
    private EnvironmentService service;

    @RequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<Environment>>> get(@RequestParam(value = "size", required = false) Integer size,
                                                                     @RequestParam(value = "page", required = false) Integer page) {
        return service.get(size, page);
    }

    @RequestMapping(value = "/{id:\\d+}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Environment>> getWithParam(@PathVariable String id) {
        return service.getWithParam(id);
    }

    @RequestMapping(method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> post(@RequestBody String body) {
        return service.post(body);
    }

    @RequestMapping(value = "/{id:\\d+}", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postWithParam(@PathVariable String id, @RequestBody String body) {
        return service.postWithParam(id, body);
    }

    @RequestMapping(method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> put(@RequestBody String body) {
        return service.put(body);
    }

    @RequestMapping(value = "/{id:\\d+}", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> putWithParam(@PathVariable String id, @RequestBody String body) {
        return service.putWithParam(id, body);
    }

    @RequestMapping(method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delete() {
        return service.delete();
    }

    @RequestMapping(value = "/{id:\\d+}", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteWithParam(@PathVariable String id) {
        return service.deleteWithParam(id);
    }

    @RequestMapping(method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> patch(@RequestBody String body) {
        return service.patch(body);
    }

    @RequestMapping(value = "/{id:\\d+}", method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> patchWithParam(@PathVariable String id, @RequestBody String body) {
        return service.patchWithParam(id, body);
    }

    @RequestMapping(method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> options() {
        return service.options();
    }

    @RequestMapping(value = "/{id:\\d+}", method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> optionsWithParam(@PathVariable String id) {
        return service.optionsWithParam(id);
    }

    @RequestMapping(method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> head() {
        return service.head();
    }

    @RequestMapping(value = "/{id:\\d+}", method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> headWithParam(@PathVariable String id) {
        return service.headWithParam(id);
    }

    @RequestMapping(method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> trace() {
        return service.trace();
    }

    @RequestMapping(value = "/{id:\\d+}", method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> traceWithParam(@PathVariable String id) {
        return service.traceWithParam(id);
    }

}
