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

package io.galeb.api.controllers;

import io.galeb.api.repository.EnvironmentRepository;
import io.galeb.core.entity.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@SuppressWarnings({"unused", "SameReturnValue"})
@RestController
@RequestMapping("/custom-search")
public class CustomSearchController {

    @Autowired
    private EnvironmentRepository environmentRepository;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/environment/findAllByVirtualhostgroupId",method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResources<Resource<Environment>>> findAllByVirtualhostgroupId(@RequestParam("vhgid") Long vhgid) {
        List<Environment> environments = environmentRepository.findAllByVirtualhostgroupId(vhgid);
        List<Resource<Environment>> resources = environments.stream().map(Resource::new).collect(Collectors.toList());
        PagedResources.PageMetadata meta = new PagedResources.PageMetadata(environments.size(), 0, environments.size());
        return ResponseEntity.ok(new PagedResources<>(resources, meta, Collections.emptyList()));
    }
}
