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

package io.galeb.router.configurations;

import io.galeb.core.enums.SystemEnv;
import io.galeb.router.handlers.RootHandler;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.xnio.Options;

@Configuration
@Order(2)
public class UndertowConfiguration {

    private final int port = Integer.parseInt(SystemEnv.ROUTER_PORT.getValue());
    private final RootHandler rootHandler;

    @Autowired
    public UndertowConfiguration(final RootHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

    @Bean
    public Undertow undertow() {
        return Undertow.builder().addHttpListener(port, "0.0.0.0", rootHandler)
                .setIoThreads(Integer.parseInt(SystemEnv.IO_THREADS.getValue()))
                .setWorkerThreads(Integer.parseInt(SystemEnv.WORKER_THREADS.getValue()))
                .setBufferSize(Integer.parseInt(SystemEnv.BUFFER_SIZE.getValue()))
                .setDirectBuffers(Boolean.parseBoolean(SystemEnv.DIRECT_BUFFER.getValue()))
                .setSocketOption(Options.BACKLOG, Integer.parseInt(SystemEnv.BACKLOG.getValue()))
                .setSocketOption(Options.KEEP_ALIVE, true)
                .setSocketOption(Options.REUSE_ADDRESSES, true)
                .setSocketOption(Options.TCP_NODELAY, true)
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setServerOption(UndertowOptions.ENABLE_STATISTICS, true)
                .build();
    }
}
