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

package io.galeb.legba.services;

import io.galeb.core.entity.VirtualHost;
import io.galeb.legba.repository.VirtualHostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CopyService {

    @Autowired
    private VirtualHostRepository virtualHostRepository;

    public List<VirtualHost> getVirtualHosts(Long envId) {
        List<VirtualHost> listVirtualHost;
        listVirtualHost = virtualHostRepository.findAllByEnvironmentId(envId);
        return listVirtualHost;
    }

}
