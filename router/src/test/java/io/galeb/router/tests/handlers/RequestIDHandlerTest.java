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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.Matchers;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import io.galeb.router.handlers.RequestIDHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.Matchers;
import org.junit.Test;

public class RequestIDHandlerTest {

    static {
        System.setProperty("REQUESTID_HEADER", "X-RID");
    }

    private final Log logger = LogFactory.getLog(this.getClass());
    @Test
    public void checkHeader() {
        RequestIDHandler requestIDHandler = new RequestIDHandler();
        ServerConnection serverConnection = mock(ServerConnection.class);
        try {
            HttpServerExchange exchange = new HttpServerExchange(serverConnection);
            requestIDHandler.handleRequest(exchange);
            assertThat(exchange.getRequestHeaders().get("X-RID"), Matchers.notNullValue());

            HttpServerExchange exchangeWithHeader = new HttpServerExchange(serverConnection);
            exchangeWithHeader.getRequestHeaders().add(new HttpString("X-RID"), "ABC");
            requestIDHandler.handleRequest(exchangeWithHeader);
            assertThat(exchangeWithHeader.getRequestHeaders().get("X-RID").getFirst(), equalTo("ABC"));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
