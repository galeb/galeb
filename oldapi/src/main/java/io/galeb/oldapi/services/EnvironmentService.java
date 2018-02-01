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

import io.galeb.core.exceptions.BadRequestException;
import io.galeb.oldapi.entities.v1.Environment;
import io.galeb.oldapi.services.components.LinkProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class EnvironmentService extends AbstractConverterService<Environment> {

    private static final Logger LOGGER = LogManager.getLogger(EnvironmentService.class);

    private final LinkProcessor linkProcessor;

    @Autowired
    public EnvironmentService(LinkProcessor linkProcessor) {
        super();
        this.linkProcessor = linkProcessor;
    }

    @Override
    protected void convertFromV2LinksToV1Links(Set<Link> links, Long id) {
        linkProcessor.add(links,"/" + getResourceName() + "/" + id + "/farms", "farms")
                     .add(links,"/" + getResourceName() + "/" + id + "/targets", "targets")
                     .remove(links, "rulesordered");
    }

    @Override
    protected String convertFromJsonStringV1ToJsonStringV2(String body) {
        Environment environmentV1 = convertFromJsonStringToV1(body);
        io.galeb.core.entity.Environment environmentV2 = new io.galeb.core.entity.Environment();
        environmentV2.setName(environmentV1.getName());

        String newBody = convertFromObjectToJsonString(environmentV2);
        if (newBody != null) {
            return newBody;
        }
        throw new BadRequestException("body fail");
    }

}
