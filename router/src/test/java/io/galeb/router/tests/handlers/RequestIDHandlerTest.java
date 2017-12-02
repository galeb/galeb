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

import io.galeb.router.handlers.RequestIDHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.util.HttpString;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class RequestIDHandlerTest {

    @Test
    public void checkHeaderDefined() {
        System.setProperty("REQUESTID_HEADER", "X-RID");

        RequestIDHandler requestIDHandler = new RequestIDHandler();
        ServerConnection serverConnection = mock(ServerConnection.class);
        try {
            HttpServerExchange exchange = new HttpServerExchange(serverConnection);
            requestIDHandler.handleRequest(exchange);
            assertThat(exchange.getRequestHeaders().get("X-RID"), Matchers.notNullValue());

            assertWithHeaderPreExisting(requestIDHandler, serverConnection);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void checkHeaderUndefined() {
        System.setProperty("REQUESTID_HEADER", "");

        RequestIDHandler requestIDHandler = new RequestIDHandler();
        ServerConnection serverConnection = mock(ServerConnection.class);
        try {
            HttpServerExchange exchange = new HttpServerExchange(serverConnection);
            requestIDHandler.handleRequest(exchange);
            assertThat(exchange.getRequestHeaders().get("X-RID"), Matchers.nullValue());

            assertWithHeaderPreExisting(requestIDHandler, serverConnection);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void checkHeaderIgnoreCase() {
        System.setProperty("REQUESTID_HEADER", "X-RID");

        RequestIDHandler requestIDHandler = new RequestIDHandler();
        ServerConnection serverConnection = mock(ServerConnection.class);
        try {
            HttpServerExchange exchange = new HttpServerExchange(serverConnection);
            requestIDHandler.handleRequest(exchange);
            assertThat(exchange.getRequestHeaders().get("x-rid"), Matchers.notNullValue());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void assertWithHeaderPreExisting(RequestIDHandler requestIDHandler, ServerConnection serverConnection) throws AssertionError {
        HttpServerExchange exchangeWithHeader = newExchangeWithHeader(serverConnection);
        try {
            requestIDHandler.handleRequest(exchangeWithHeader);
            assertThat(exchangeWithHeader.getRequestHeaders().get("X-RID").getFirst(), equalTo("ABC"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private HttpServerExchange newExchangeWithHeader(ServerConnection serverConnection) {
        HttpServerExchange exchangeWithHeader = new HttpServerExchange(serverConnection);
        exchangeWithHeader.getRequestHeaders().add(new HttpString("X-RID"), "ABC");
        return exchangeWithHeader;
    }
}
