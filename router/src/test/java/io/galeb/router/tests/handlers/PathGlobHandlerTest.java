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

package io.galeb.router.tests.handlers;

import io.galeb.router.handlers.PathGlobHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class PathGlobHandlerTest {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void pathOrderedTest() {
        PathGlobHandler.PathOrdered pathOrdered1 = new PathGlobHandler.PathOrdered("x", 0);
        assertThat(pathOrdered1.compareTo(null), is(-1));

        PathGlobHandler.PathOrdered pathOrdered2 = new PathGlobHandler.PathOrdered("x", 0);

        assertThat(pathOrdered1.equals(pathOrdered2), is(true));

        PathGlobHandler.PathOrdered pathOrdered3 = new PathGlobHandler.PathOrdered("y", 0);

        assertThat(pathOrdered1.equals(pathOrdered3), is(false));
        assertThat(pathOrdered1.compareTo(pathOrdered3), is(-1));

        PathGlobHandler.PathOrdered pathOrdered4 = new PathGlobHandler.PathOrdered("x", 1);

        assertThat(pathOrdered1.equals(pathOrdered4), is(true));
        assertThat(pathOrdered1.compareTo(pathOrdered4), is(-1));
        assertThat(pathOrdered4.compareTo(pathOrdered1), is(1));

        PathGlobHandler.PathOrdered pathOrdered5 = new PathGlobHandler.PathOrdered("y", 1);

        assertThat(pathOrdered5.equals(pathOrdered4), is(false));
        assertThat(pathOrdered1.compareTo(pathOrdered5), is(-1));
        assertThat(pathOrdered5.compareTo(pathOrdered1), is(1));
    }

    @Test
    public void addRemoveTest() {
        PathGlobHandler pathGlobHandler = new PathGlobHandler();

        pathGlobHandler.addPath("x", 0, ResponseCodeHandler.HANDLE_200);
        pathGlobHandler.addPath("x", 0, ResponseCodeHandler.HANDLE_200);
        pathGlobHandler.addPath("y", 0, ResponseCodeHandler.HANDLE_200);
        pathGlobHandler.addPath("z", 1, ResponseCodeHandler.HANDLE_200);
        pathGlobHandler.addPath("w", 1, ResponseCodeHandler.HANDLE_200);

        try {
            assertThat(pathGlobHandler.getPaths().entrySet(), Matchers.hasSize(4));

            assertThat(pathGlobHandler.contains("x"), is(true));
            pathGlobHandler.removePath("x");
            assertThat(pathGlobHandler.getPaths().entrySet(), Matchers.hasSize(3));
            assertThat(pathGlobHandler.getPaths().keySet(), Matchers.contains(
                    new PathGlobHandler.PathOrdered("y", 0),
                    new PathGlobHandler.PathOrdered("w", 1),
                    new PathGlobHandler.PathOrdered("z", 1))
            );

            assertThat(pathGlobHandler.contains("z"), is(true));
            pathGlobHandler.removePath("z");
            assertThat(pathGlobHandler.getPaths().entrySet(), Matchers.hasSize(2));
            assertThat(pathGlobHandler.getPaths().keySet(), Matchers.contains(
                    new PathGlobHandler.PathOrdered("y", 0),
                    new PathGlobHandler.PathOrdered("w", 1)
                    )
            );

            assertThat(pathGlobHandler.contains("y"), is(true));
            pathGlobHandler.removePath("y");
            assertThat(pathGlobHandler.getPaths().entrySet(), Matchers.hasSize(1));
            assertThat(pathGlobHandler.getPaths().keySet(), Matchers.contains(
                    new PathGlobHandler.PathOrdered("w", 1))
            );

            pathGlobHandler.removePath("k");
            assertThat(pathGlobHandler.getPaths().entrySet(), Matchers.hasSize(1));
            assertThat(pathGlobHandler.getPaths().keySet(), Matchers.contains(
                    new PathGlobHandler.PathOrdered("w", 1))
            );

            assertThat(pathGlobHandler.contains("w"), is(true));
            pathGlobHandler.removePath("w");
            assertThat(pathGlobHandler.getPaths().entrySet(), Matchers.hasSize(0));

        } catch (AssertionError e) {
            logger.error("size wrong. pathGlobHandler registered paths are:");
            pathGlobHandler.getPaths().forEach((k, v) -> logger.error(k.getPath()));
            throw e;
        }
    }

    @Test
    public void checkMatch() {
        final AtomicReference<String> result = new AtomicReference<>("default");

        HttpHandler defaultHandler = mock(HttpHandler.class);
        PathGlobHandler pathGlobHandler = new PathGlobHandler();
        pathGlobHandler.setDefaultHandler(defaultHandler);

        pathGlobHandler.addPath("/x", 0, exchange -> result.set("x"));
        pathGlobHandler.addPath("/y", 0, exchange -> result.set("y"));
        pathGlobHandler.addPath("/z", 0, exchange -> result.set("z"));
        pathGlobHandler.addPath("/w", 0, exchange -> result.set("w"));
        pathGlobHandler.addPath("/1", 1, exchange -> result.set("1"));
        pathGlobHandler.addPath("/2", 2, exchange -> result.set("2"));
        pathGlobHandler.addPath("/3", 3, exchange -> result.set("3"));
        pathGlobHandler.addPath("/4", 4, exchange -> result.set("4"));
        pathGlobHandler.addPath("/5*", 4, exchange -> result.set("5"));
        pathGlobHandler.addPath("/6/*", Integer.MAX_VALUE - 1, exchange -> result.set("6"));
        pathGlobHandler.addPath("/7/*.json", Integer.MAX_VALUE - 1, exchange -> result.set("7"));
        pathGlobHandler.addPath("/", Integer.MAX_VALUE, exchange -> result.set("slash"));

        ServerConnection serverConnection = mock(ServerConnection.class);
        try {
            HttpServerExchange exchangeNotMatch = new HttpServerExchange(serverConnection);
            exchangeNotMatch.setRelativePath(UUID.randomUUID().toString());
            HttpServerExchange exchangeX = new HttpServerExchange(serverConnection);
            exchangeX.setRelativePath("/x");
            HttpServerExchange exchangeY = new HttpServerExchange(serverConnection);
            exchangeY.setRelativePath("/y");
            HttpServerExchange exchangeZ = new HttpServerExchange(serverConnection);
            exchangeZ.setRelativePath("/z");
            HttpServerExchange exchangeW = new HttpServerExchange(serverConnection);
            exchangeW.setRelativePath("/w");
            HttpServerExchange exchange1 = new HttpServerExchange(serverConnection);
            exchange1.setRelativePath("/1");
            HttpServerExchange exchange2 = new HttpServerExchange(serverConnection);
            exchange2.setRelativePath("/2");
            HttpServerExchange exchange3 = new HttpServerExchange(serverConnection);
            exchange3.setRelativePath("/3");
            HttpServerExchange exchange4 = new HttpServerExchange(serverConnection);
            exchange4.setRelativePath("/4");
            HttpServerExchange exchange5 = new HttpServerExchange(serverConnection);
            exchange5.setRelativePath("/555");
            HttpServerExchange exchange6 = new HttpServerExchange(serverConnection);
            exchange6.setRelativePath("/6/xpto");
            HttpServerExchange exchange7 = new HttpServerExchange(serverConnection);
            exchange7.setRelativePath("/7/xpto/test.json");
            HttpServerExchange exchangeSlash = new HttpServerExchange(serverConnection);
            exchangeSlash.setRelativePath("/");

            pathGlobHandler.handleRequest(exchangeNotMatch);
            assertThat(result.get(), equalTo("default"));

            pathGlobHandler.handleRequest(exchangeX);
            assertThat(result.get(), equalTo("x"));

            pathGlobHandler.handleRequest(exchangeY);
            assertThat(result.get(), equalTo("y"));

            pathGlobHandler.handleRequest(exchangeZ);
            assertThat(result.get(), equalTo("z"));

            pathGlobHandler.handleRequest(exchangeW);
            assertThat(result.get(), equalTo("w"));

            pathGlobHandler.handleRequest(exchange1);
            assertThat(result.get(), equalTo("1"));

            pathGlobHandler.handleRequest(exchange2);
            assertThat(result.get(), equalTo("2"));

            pathGlobHandler.handleRequest(exchange3);
            assertThat(result.get(), equalTo("3"));

            pathGlobHandler.handleRequest(exchange4);
            assertThat(result.get(), equalTo("4"));

            pathGlobHandler.handleRequest(exchange2);
            assertThat(result.get(), equalTo("2"));

            pathGlobHandler.handleRequest(exchange1);
            assertThat(result.get(), equalTo("1"));

            pathGlobHandler.handleRequest(exchange5);
            assertThat(result.get(), equalTo("5"));

            pathGlobHandler.handleRequest(exchange6);
            assertThat(result.get(), equalTo("6"));

            pathGlobHandler.handleRequest(exchange7);
            assertThat(result.get(), equalTo("7"));

            pathGlobHandler.handleRequest(exchangeSlash);
            assertThat(result.get(), equalTo("slash"));

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (AssertionError assertionError) {
            pathGlobHandler.getPaths().forEach((k, v) -> logger.error(k.getPath() + " -> " + k.getOrder()));
            throw assertionError;
        }
    }
}
