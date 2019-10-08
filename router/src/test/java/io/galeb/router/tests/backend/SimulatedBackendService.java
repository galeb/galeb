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

package io.galeb.router.tests.backend;

import io.galeb.router.tests.client.HttpClient;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@Service
public class SimulatedBackendService {

    @SuppressWarnings("unused")
    public enum ResponseBehavior {
        BROKEN(ResponseCodeHandler.HANDLE_500),
        FASTTER(ResponseCodeHandler.HANDLE_200),
        FAST(exchange -> exchange.getResponseSender().send("A")),
        SLOW(exchange -> { Thread.sleep(5000); ResponseCodeHandler.HANDLE_200.handleRequest(exchange);}),
        HUGE(exchange -> {
            final byte[] bytes = "A".getBytes(Charset.defaultCharset());
            int count = 1024 * 1024 * 100; // 100Mb
            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length * count);
            while (byteBuffer.hasRemaining()) byteBuffer.put(bytes);
            exchange.getResponseSender().send(byteBuffer);
        });

        private final HttpHandler handler;
        final HttpHandler getHandler() {
            return handler;
        }

        ResponseBehavior(final HttpHandler handler) {
            this.handler = handler;
        }
    }

    private final Log logger = LogFactory.getLog(this.getClass());
    private final HttpClient client;

    private ResponseBehavior behavior = null;

    private Undertow undertow;

    @Autowired
    public SimulatedBackendService(final HttpClient client) {
        this.client = client;
    }

    public SimulatedBackendService setResponseBehavior(ResponseBehavior behavior) {
        this.behavior = behavior;
        int backendPort = 8080;
        this.undertow = Undertow.builder()
                                .setServerOption(UndertowOptions.ALLOW_UNESCAPED_CHARACTERS_IN_URL, true)
                                .addHttpListener(backendPort, "0.0.0.0", behavior.getHandler()).build();
        return this;
    }

    public void start() {
        try {
            client.get("http://127.0.0.1:" + 8080 + "/");
        } catch (Exception ignore) {
            if (behavior != null && behavior != ResponseBehavior.BROKEN) {
                undertow.start();
                logger.info(this.getClass().getSimpleName() + " started");
            } else {
                logger.info(this.getClass().getSimpleName() + " not started");
            }
        }
    }

    public void stop() {
        if (undertow != null) {
            undertow.stop();
        }
        logger.info(this.getClass().getSimpleName() + " stopped");
    }
}
