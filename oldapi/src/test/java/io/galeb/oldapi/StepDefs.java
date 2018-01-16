/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
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

package io.galeb.oldapi;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

@SuppressWarnings("Duplicates")
@ContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:application.yml")
public class StepDefs {

    private static final Log LOGGER     = LogFactory.getLog(StepDefs.class);
    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @LocalServerPort
    private int port;

    private String resourceUrl;

    @Before
    public void setup() {
        resourceUrl = "http://127.0.0.1:" + port;
    }

    @Then("simple GET request to (.*)")
    public void simpleRequest(String path) {
        LOGGER.warn("resourceUrl: " + resourceUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl + path, String.class);
        Assert.isTrue(response.getStatusCodeValue() == 200, "Not OK");
    }


}
