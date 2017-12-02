
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

package io.galeb.router.tests.cucumber;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.eo.Do;
import io.galeb.core.enums.SystemEnv;
import io.galeb.router.Application;
import io.galeb.router.tests.backend.SimulatedBackendService;
import io.galeb.router.tests.client.HttpClient;
import io.galeb.router.tests.client.JmxClientService;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.uri.Uri;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = { Application.class },
        loader = SpringBootContextLoader.class
)
@ActiveProfiles({ "test" })
@Ignore
public class StepDefs {

    private final Log logger = LogFactory.getLog(this.getClass());

    private Response response;
    private String method;
    private Uri uri = Uri.create("http://127.0.0.1:" + SystemEnv.ROUTER_PORT.getValue());
    private final InetAddress address= InetAddress.getLocalHost();
    private final HttpHeaders headers = new DefaultHttpHeaders();

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private SimulatedBackendService backendService;

    @Autowired
    private HttpClient client;

    @Autowired
    private JmxClientService jmxClientService;

    private long requestTime = 0L;

    public StepDefs() throws UnknownHostException {}

    @Before
    public void setUp() {
        response = null;
    }

    @PostConstruct
    void init() {
        logger.info("Using " + httpClient.getClass().getName());
    }

    private void executeRequest() throws InterruptedException, java.util.concurrent.ExecutionException {
        long start = System.currentTimeMillis();
        this.response = client.execute(new RequestBuilder()
                .setHeaders(headers).setAddress(address).setMethod(method).setUri(uri));
        this.requestTime = System.currentTimeMillis() - start;
        logger.info("request time (ms): " + requestTime);
    }

    @Given("^a (.+) host request to (.+) backend$")
    public void withHostRequester(String expression, String backendBehavior) throws Throwable {
        response = null;
        backendService.stop();
        backendService.setResponseBehavior(SimulatedBackendService.ResponseBehavior.valueOf(backendBehavior)).start();
        this.headers.remove("host");
        this.headers.add("host", ("valid".equals(expression) ? "test.com" : expression));
    }

    @Do("^with headers:$")
    public void withHeaders(final Map<String, String> headers) throws Throwable {
        for (Map.Entry<String, String> header: headers.entrySet()) {
            this.headers.add(header.getKey(), header.getValue());
        }
    }

    @Do("^Do (.+) (.+)$")
    public void sendMethodPath(String method, String path) throws Throwable {
        this.uri = Uri.create("http://127.0.0.1:" + SystemEnv.ROUTER_PORT.getValue() + path);
        this.method = method;
    }

    @Do("^the response status is (\\d+)$")
    public void theResponseStatusIs(int status) throws Throwable {
        executeRequest();
        assertThat(response.getStatusCode(), is(status));
    }

    @Do("^body is (\\w* )?(.+)")
    public void bodyIs(String inverter, String body) throws Throwable {
        assertThat(response.getResponseBody(), inverter != null ? not(equalTo(body)) : equalTo(body));
    }

    @Do("^jmx has ActiveConnections$")
    public void jmxHasActiveConnections() {
        if (jmxClientService.isEnabled()) {
            assertThat(jmxClientService.getValue("ActiveConnections"), notNullValue());
        }
    }

    @Do("^jmx has ActiveRequests$")
    public void jmxHasActiveRequests() {
        if (jmxClientService.isEnabled()) {
            assertThat(jmxClientService.getValue("ActiveRequests"), notNullValue());
        }
    }

    @And("^wait (\\d+) ms$")
    public void waitMs(long timeWait) throws Throwable {
        Thread.sleep(timeWait);
    }
}
