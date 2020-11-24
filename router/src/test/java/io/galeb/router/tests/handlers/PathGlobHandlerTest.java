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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.InetSocketAddress;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableSortedMap;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.galeb.router.ResponseCodeOnError.Header;
import io.galeb.router.handlers.PathGlobHandler;
import io.galeb.router.handlers.PathGlobHandler.PathOrdered;
import io.undertow.server.Connectors;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

public class PathGlobHandlerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
    public void handlerRoutesEmptyTest() {
        ImmutableSortedMap<PathOrdered, HttpHandler> paths = ImmutableSortedMap.<PathOrdered, HttpHandler>naturalOrder()
                .build();
        PathGlobHandler handler = new PathGlobHandler(paths);

        HttpServerExchange exchange = createMockExchange("/");
        try {
            handler.handleRequest(exchange);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }

        Assert.assertEquals("", exchange.getResponseHeaders().get(Header.X_GALEB_ERROR), "RULES_EMPTY");
        Assert.assertEquals("", exchange.getStatusCode(), 503);
    }

    @Test
    public void handlerRoutesCorrectlyTest() {
        final AtomicReference<String> result = new AtomicReference<>("default");
        Map<PathOrdered, HttpHandler> allPaths = new HashMap<>();

        allPaths.put(new PathOrdered("/x*", 0), exchange -> result.set("/x"));
        allPaths.put(new PathOrdered("/y*", 0), exchange -> result.set("/y"));
        allPaths.put(new PathOrdered("/z*", 0), exchange -> result.set("/z"));
        allPaths.put(new PathOrdered("/w*", 0), exchange -> result.set("/w"));
        allPaths.put(new PathOrdered("/1*", 1), exchange -> result.set("/1"));
        allPaths.put(new PathOrdered("/2*", 2), exchange -> result.set("/2"));
        allPaths.put(new PathOrdered("/3*", 3), exchange -> result.set("/3"));
        allPaths.put(new PathOrdered("/4/*", Integer.MAX_VALUE - 1), exchange -> result.set("/4"));
        allPaths.put(new PathOrdered("/5/*.json", Integer.MAX_VALUE - 1), exchange -> result.set("/5"));
        allPaths.put(new PathOrdered("/", Integer.MAX_VALUE), exchange -> result.set("slash"));

        ImmutableSortedMap<PathOrdered, HttpHandler> paths = ImmutableSortedMap.<PathOrdered, HttpHandler>naturalOrder()
                .putAll(allPaths).build();
        PathGlobHandler handler = new PathGlobHandler(paths);
        handler.setDefaultHandler(exchange -> result.set("default-handler"));

        Map<String, String> tests = Map.ofEntries(new AbstractMap.SimpleEntry<String, String>("/x", "/x"),
                new AbstractMap.SimpleEntry<String, String>("/y", "/y"),
                new AbstractMap.SimpleEntry<String, String>("/z", "/z"),
                new AbstractMap.SimpleEntry<String, String>("/w", "/w"),
                new AbstractMap.SimpleEntry<String, String>("/1", "/1"),
                new AbstractMap.SimpleEntry<String, String>("/2", "/2"),
                new AbstractMap.SimpleEntry<String, String>("/3", "/3"),
                new AbstractMap.SimpleEntry<String, String>("/4/asdf", "/4"),
                new AbstractMap.SimpleEntry<String, String>("/5/asdf.json", "/5"),
                new AbstractMap.SimpleEntry<String, String>("/", "slash"),
                new AbstractMap.SimpleEntry<String, String>("/tests", "default-handler"));

        tests.forEach((url, expected) -> {
            HttpServerExchange exchange = createMockExchange(url);
            try {
                handler.handleRequest(exchange);
            } catch (Exception e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
            Assert.assertEquals("", result.get(), expected);
        });

    }

    private HttpServerExchange createMockExchange(String url) {
        HttpServerExchange exchange = new HttpServerExchange(Mockito.mock(ServerConnection.class), getRequestHeaders(),
            new HeaderMap(), 0);
        exchange.setSourceAddress(new InetSocketAddress("1.2.3.4", 44444));
        exchange.setRequestMethod(HttpString.tryFromString("GET"));
        exchange.setRelativePath(url);
        exchange.setProtocol(HttpString.tryFromString("HTTP"));
        exchange.setStatusCode(200);
        Connectors.setRequestStartTime(exchange);

        return exchange;
    }

    private HeaderMap getRequestHeaders() {
        HeaderMap headerMap = new HeaderMap();
        headerMap.add(HttpString.tryFromString("HOST"), "vhost.host.virtual");
        return headerMap;
    }

    // @Test
    // public void checkMatch() {
    // final AtomicReference<String> result = new AtomicReference<>("default");

    // HttpHandler defaultHandler = mock(HttpHandler.class);
    // PathGlobHandler pathGlobHandler = new PathGlobHandler();
    // pathGlobHandler.setDefaultHandler(defaultHandler);

    // pathGlobHandler.addPath("/x", 0, exchange -> result.set("x"));
    // pathGlobHandler.addPath("/y", 0, exchange -> result.set("y"));
    // pathGlobHandler.addPath("/z", 0, exchange -> result.set("z"));
    // pathGlobHandler.addPath("/w", 0, exchange -> result.set("w"));
    // pathGlobHandler.addPath("/1", 1, exchange -> result.set("1"));
    // pathGlobHandler.addPath("/2", 2, exchange -> result.set("2"));
    // pathGlobHandler.addPath("/3", 3, exchange -> result.set("3"));
    // pathGlobHandler.addPath("/4", 4, exchange -> result.set("4"));
    // pathGlobHandler.addPath("/5*", 4, exchange -> result.set("5"));
    // pathGlobHandler.addPath("/6/*", Integer.MAX_VALUE - 1, exchange ->
    // result.set("6"));
    // pathGlobHandler.addPath("/7/*.json", Integer.MAX_VALUE - 1, exchange ->
    // result.set("7"));
    // pathGlobHandler.addPath("/", Integer.MAX_VALUE, exchange ->
    // result.set("slash"));

    // ServerConnection serverConnection = mock(ServerConnection.class);
    // try {
    // HttpServerExchange exchangeNotMatch = new
    // HttpServerExchange(serverConnection);
    // exchangeNotMatch.setRelativePath(UUID.randomUUID().toString());
    // HttpServerExchange exchangeX = new HttpServerExchange(serverConnection);
    // exchangeX.setRelativePath("/x");
    // HttpServerExchange exchangeY = new HttpServerExchange(serverConnection);
    // exchangeY.setRelativePath("/y");
    // HttpServerExchange exchangeZ = new HttpServerExchange(serverConnection);
    // exchangeZ.setRelativePath("/z");
    // HttpServerExchange exchangeW = new HttpServerExchange(serverConnection);
    // exchangeW.setRelativePath("/w");
    // HttpServerExchange exchange1 = new HttpServerExchange(serverConnection);
    // exchange1.setRelativePath("/1");
    // HttpServerExchange exchange2 = new HttpServerExchange(serverConnection);
    // exchange2.setRelativePath("/2");
    // HttpServerExchange exchange3 = new HttpServerExchange(serverConnection);
    // exchange3.setRelativePath("/3");
    // HttpServerExchange exchange4 = new HttpServerExchange(serverConnection);
    // exchange4.setRelativePath("/4");
    // HttpServerExchange exchange5 = new HttpServerExchange(serverConnection);
    // exchange5.setRelativePath("/555");
    // HttpServerExchange exchange6 = new HttpServerExchange(serverConnection);
    // exchange6.setRelativePath("/6/xpto");
    // HttpServerExchange exchange7 = new HttpServerExchange(serverConnection);
    // exchange7.setRelativePath("/7/xpto/test.json");
    // HttpServerExchange exchangeSlash = new HttpServerExchange(serverConnection);
    // exchangeSlash.setRelativePath("/");

    // pathGlobHandler.handleRequest(exchangeNotMatch);
    // assertThat(result.get(), equalTo("default"));

    // pathGlobHandler.handleRequest(exchangeX);
    // assertThat(result.get(), equalTo("x"));

    // pathGlobHandler.handleRequest(exchangeY);
    // assertThat(result.get(), equalTo("y"));

    // pathGlobHandler.handleRequest(exchangeZ);
    // assertThat(result.get(), equalTo("z"));

    // pathGlobHandler.handleRequest(exchangeW);
    // assertThat(result.get(), equalTo("w"));

    // pathGlobHandler.handleRequest(exchange1);
    // assertThat(result.get(), equalTo("1"));

    // pathGlobHandler.handleRequest(exchange2);
    // assertThat(result.get(), equalTo("2"));

    // pathGlobHandler.handleRequest(exchange3);
    // assertThat(result.get(), equalTo("3"));

    // pathGlobHandler.handleRequest(exchange4);
    // assertThat(result.get(), equalTo("4"));

    // pathGlobHandler.handleRequest(exchange2);
    // assertThat(result.get(), equalTo("2"));

    // pathGlobHandler.handleRequest(exchange1);
    // assertThat(result.get(), equalTo("1"));

    // pathGlobHandler.handleRequest(exchange5);
    // assertThat(result.get(), equalTo("5"));

    // pathGlobHandler.handleRequest(exchange6);
    // assertThat(result.get(), equalTo("6"));

    // pathGlobHandler.handleRequest(exchange7);
    // assertThat(result.get(), equalTo("7"));

    // pathGlobHandler.handleRequest(exchangeSlash);
    // assertThat(result.get(), equalTo("slash"));

    // } catch (Exception e) {
    // logger.error(ExceptionUtils.getStackTrace(e));
    // } catch (AssertionError assertionError) {
    // pathGlobHandler.getPaths().forEach((k, v) -> logger.error(k.getPath() + " ->
    // " + k.getOrder()));
    // throw assertionError;
    // }
    // }
}
