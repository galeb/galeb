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

import io.galeb.oldapi.entities.v1.VirtualHost;
import io.galeb.oldapi.services.http.HttpClientService;
import io.galeb.oldapi.services.utils.LinkProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class VirtualHostService extends AbstractConverterService<VirtualHost> {

    private static final Logger LOGGER = LogManager.getLogger(VirtualHostService.class);
    private final LinkProcessor linkProcessor;

    @Autowired
    public VirtualHostService(LinkProcessor linkProcessor, HttpClientService httpClientService, @Value("${api.url}") String apiUrl) {
        super(linkProcessor, httpClientService);
        this.resourceUrlBase = apiUrl + "/" + getResourceName();
        this.linkProcessor = linkProcessor;
    }

    @Override
    void fixV1Links(Set<Link> links, Long id) {
        linkProcessor.add(links,"/" + getResourceName() + "/" + id + "/ruleDefault", "ruleDefault")
                     .add(links,"/" + getResourceName() + "/" + id + "/rules", "rules")
                     .remove(links, "rulesOrdered")
                     .remove(links, "virtualhostgroup");
    }

}
