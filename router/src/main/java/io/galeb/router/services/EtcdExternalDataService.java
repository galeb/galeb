/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

package io.galeb.router.services;

import io.galeb.router.kv.EtcdClient;
import io.galeb.router.kv.EtcdExternalData;
import io.galeb.router.kv.EtcdNode;
import io.galeb.router.kv.ExternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class EtcdExternalDataService implements ExternalDataService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EtcdClient client;

    @Autowired
    public EtcdExternalDataService(final EtcdClient etcdClient) {
        this.client = etcdClient;
    }

    @Override
    public List<ExternalData> listFrom(String key) {
        return listFrom(key, false);
    }

    @Override
    public List<ExternalData> listFrom(String key, boolean recursive) {
        return listFrom(node(key, recursive));
    }

    @Override
    public List<ExternalData> listFrom(ExternalData node) {
        return node.getNodes();
    }

    @Override
    public ExternalData node(String key) {
        return node(key, false);
    }

    @Override
    public ExternalData node(String key, boolean recursive) {
        return node(key, recursive, ExternalData.Generic.NULL);
    }

    @Override
    public ExternalData node(String key, ExternalData.Generic def) {
        return node(key, false, def);
    }

    @Override
    public synchronized ExternalData node(String key, boolean recursive, ExternalData.Generic def) {
        try {
            final EtcdNode node = client.get(key, recursive).getNode();
            final ExternalData data = node != null ? new EtcdExternalData(node) : def.instance();
            logger.debug("GET " + key + ": " +  "ExternalData(value=" + data.getValue() + ", dir=" + data.isDir() + ")");
            return data;
        } catch (Exception e) {
            logger.warn("GET " + key + " FAIL: " + e.getMessage());
            return def.instance();
        }
    }

    @Override
    public synchronized boolean exist(String key) {
        try {
            final EtcdNode node = client.get(key, false).getNode();
            return node != null && (node.getValue() != null || node.isDir());
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

}
