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

import static io.galeb.router.tests.mocks.MockHttpServerExchange.createMockExchange;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableSortedMap;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.galeb.router.ResponseCodeOnError.Header;
import io.galeb.router.handlers.PathGlobHandler;
import io.galeb.router.handlers.PathGlobHandler.PathOrdered;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

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

        Assert.assertEquals("", exchange.getResponseHeaders().get(Header.X_GALEB_ERROR).get(0), "RULES_EMPTY");
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
        allPaths.put(new PathOrdered("/exception/*", 0), exchange -> {
            throw new Exception();
        });
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
                new AbstractMap.SimpleEntry<String, String>("/tests", "default-handler"),
                new AbstractMap.SimpleEntry<String, String>("/exception/a", "default-handler"));

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
}
