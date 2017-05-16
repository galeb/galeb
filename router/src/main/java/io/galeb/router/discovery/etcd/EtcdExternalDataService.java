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

package io.galeb.router.discovery.etcd;

import io.galeb.core.enums.SystemEnv;
import io.galeb.core.logutils.ErrorLogger;
import io.galeb.router.discovery.ExternalData;
import io.galeb.router.discovery.ExternalDataService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class EtcdExternalDataService implements ExternalDataService {

    private static final long REGISTER_TTL = 30L; // seconds

    private final String registerRootPath = SystemEnv.ETCD_REGISTER_PATH.getValue();
    private final String environment = SystemEnv.ENVIRONMENT_NAME.getValue();
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
            final EtcdNode node = client.get(key).getNode();
            return node != null && (node.getValue() != null || node.isDir());
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    public void put(EtcdNode node) {
        try {
            client.put(node);
        } catch (ExecutionException | InterruptedException e) {
            ErrorLogger.logError(e, this.getClass());
        }
    }

    @Override
    public void register() {
        final List<String> ipList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                NetworkInterface localInterface = ifs.nextElement();
                if (!localInterface.isLoopback() && localInterface.isUp()) {
                    Enumeration<InetAddress> ips = localInterface.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        InetAddress ipaddress = ips.nextElement();
                        if (ipaddress instanceof Inet4Address) {
                            ipList.add(ipaddress.getHostAddress());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        String ip = String.join("-", ipList);
        if (ip == null || "".equals(ip)) {
            ip = "undef-" + System.currentTimeMillis();
        }
        String ketDiscoveryService = registerRootPath + "/" + environment + "/" + ip.replaceAll("[:%]", "");
        EtcdNode node = new EtcdNode();
        node.setKey(ketDiscoveryService);
        node.setTtl(REGISTER_TTL);
        node.setValue(String.valueOf(System.currentTimeMillis()));
        try {
            client.put(node);
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
        }
    }

    @Override
    public List<String> members() {
        try {
            EtcdResponse etcdResponse = client.get(SystemEnv.ETCD_REGISTER_PATH.getValue());
            EtcdNode node = Optional.ofNullable(etcdResponse.getNode()).orElse(new EtcdNode());
            if (node.isDir()) {
                return Optional.ofNullable(node.getNodes()).orElse(Collections.emptyList()).stream().map(EtcdNode::getKey)
                        .collect(Collectors.toList());
            }
            return Collections.singletonList("myself");
        } catch (ExecutionException | InterruptedException e) {
            ErrorLogger.logError(e, this.getClass());
            return Collections.singletonList("myself");
        }
    }
}
