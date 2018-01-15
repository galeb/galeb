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

import io.galeb.oldapi.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService service;

    @RequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> get() {
        return service.get();
    }

    @RequestMapping(value = "/{param:.+}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getWithParam(@PathVariable String param) {
        return service.getWithParam(param);
    }

    @RequestMapping(method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> post(@RequestBody String body) {
        return service.post(body);
    }

    @RequestMapping(value = "/{param:.+}", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postWithParam(@PathVariable String param, @RequestBody String body) {
        return service.postWithParam(param, body);
    }

    @RequestMapping(method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> put(@RequestBody String body) {
        return service.put(body);
    }

    @RequestMapping(value = "/{param:.+}", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> putWithParam(@PathVariable String param, @RequestBody String body) {
        return service.putWithParam(param, body);
    }

    @RequestMapping(method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delete() {
        return service.delete();
    }

    @RequestMapping(value = "/{param:.+}", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteWithParam(@PathVariable String param) {
        return service.deleteWithParam(param);
    }

    @RequestMapping(method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> patch(@RequestBody String body) {
        return service.patch(body);
    }

    @RequestMapping(value = "/{param:.+}", method = PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> patchWithParam(@PathVariable String param, @RequestBody String body) {
        return service.patchWithParam(param, body);
    }

    @RequestMapping(method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> options() {
        return service.options();
    }

    @RequestMapping(value = "/{param:.+}", method = OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> optionsWithParam(@PathVariable String param) {
        return service.optionsWithParam(param);
    }

    @RequestMapping(method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> head() {
        return service.head();
    }

    @RequestMapping(value = "/{param:.+}", method = HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> headWithParam(@PathVariable String param) {
        return service.headWithParam(param);
    }

    @RequestMapping(method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> trace() {
        return service.trace();
    }

    @RequestMapping(value = "/{param:.+}", method = TRACE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> traceWithParam(@PathVariable String param) {
        return service.traceWithParam(param);
    }

}
