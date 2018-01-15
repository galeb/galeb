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

import io.galeb.oldapi.services.FarmService;
import io.galeb.oldapi.v1entities.Farm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/farm")
public class FarmController {

    @Autowired
    private FarmService service;

    @RequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> get() {
        return service.get();
    }

    @RequestMapping(value = "/{param:.+}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> getWithParam(@PathVariable String param) {
        return service.getWithParam(param);
    }

    @RequestMapping(method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> post() {
        return service.post();
    }

    @RequestMapping(value = "/{param:.+}", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> postWithParam(@PathVariable String param) {
        return service.postWithParam(param);
    }

    @RequestMapping(method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> put() {
        return service.put();
    }

    @RequestMapping(value = "/{param:.+}", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> putWithParam(@PathVariable String param) {
        return service.putWithParam(param);
    }

    @RequestMapping(method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> delete() {
        return service.delete();
    }

    @RequestMapping(value = "/{param:.+}", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> deleteWithParam(@PathVariable String param) {
        return service.deleteWithParam(param);
    }

    @RequestMapping(method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> patch() {
        return service.patch();
    }

    @RequestMapping(value = "/{param:.+}", method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> patchWithParam(@PathVariable String param) {
        return service.patchWithParam(param);
    }

    @RequestMapping(method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> options() {
        return service.options();
    }

    @RequestMapping(value = "/{param:.+}", method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> optionsWithParam(@PathVariable String param) {
        return service.optionsWithParam(param);
    }

    @RequestMapping(method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> head() {
        return service.head();
    }

    @RequestMapping(value = "/{param:.+}", method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> headWithParam(@PathVariable String param) {
        return service.headWithParam(param);
    }

    @RequestMapping(method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> trace() {
        return service.trace();
    }

    @RequestMapping(value = "/{param:.+}", method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farm> traceWithParam(@PathVariable String param) {
        return service.traceWithParam(param);
    }

}
