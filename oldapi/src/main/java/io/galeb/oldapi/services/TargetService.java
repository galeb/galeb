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

import io.galeb.core.entity.AbstractEntity;
import io.galeb.oldapi.entities.v1.Target;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

@Service
public class TargetService extends AbstractConverterService<Target> {

    private static final Logger LOGGER = LogManager.getLogger(TargetService.class);

    @Override
    protected Set<Resource<Target>> convertResources(ArrayList<LinkedHashMap> v2s) {
        return null;
    }

    @Override
    protected Target convertResource(LinkedHashMap resource, Class<? extends AbstractEntity> v2entityClass) throws IOException {
        return null;
    }

}
