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

package io.galeb.api.cucumber;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RedirectConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import io.galeb.api.Application;
import io.galeb.core.entity.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jayway.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasToString;

@ContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:application.yml")
public class StepDefs {

    private static final Flyway FLYWAY  = new Flyway();
    private static final Log LOGGER     = LogFactory.getLog(StepDefs.class);

    @LocalServerPort
    private int port;

    @PersistenceContext
    private EntityManager em;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${auth.localtoken}")
    private String localAdminToken;

    private String userLocal;
    private String tokenLocal;

    private static final Gson jsonParser = new GsonBuilder().setPrettyPrinting().create();

    private RequestSpecification request;
    private ValidatableResponse response;

    private RedirectConfig redirectConfig = RestAssuredConfig.config().getRedirectConfig().followRedirects(false);
    private RestAssuredConfig restAssuredConfig = RestAssuredConfig.config().redirect(redirectConfig);

    @Before
    public void init() throws Throwable {
        LOGGER.info("Initializing FLYWAY...");
        FLYWAY.setDataSource(dbUrl, dbUsername, dbPassword);
        FLYWAY.clean();
        FLYWAY.migrate();
        createUsers();
        LOGGER.info("Finish FLYWAY!");
    }

    private void createUsers() throws Throwable {
        request = with().port(port).config(restAssuredConfig).contentType("application/json").auth().preemptive().basic("admin", "pass");

        Map<String, String> mapAccountJson = new HashMap<>();
        mapAccountJson.put("username", "userlocal");
        mapAccountJson.put("email", "userlocal@userlocal.com");
        requestJsonBodyHas(mapAccountJson);
        sendMethodPath("POST", "/account");
        System.out.println(response.extract().body().jsonPath().get().toString());

        Map<String, String> mapTeamJson = new HashMap<>();
        mapTeamJson.put("name", "teamlocal");
        mapTeamJson.put("accounts", "[Account=userlocal]");
        requestJsonBodyHas(mapTeamJson);
        sendMethodPath("POST", "/team");

        Map<String, String> mapRoleGroupJson = new HashMap<>();
        mapRoleGroupJson.put("teams", "[Team=teamlocal]");
        requestJsonBodyHas(mapRoleGroupJson);
        sendMethodPath("PATCH", "/rolegroup/2");

        sendMethodPath("GET", "/account/1?projection=apitoken");
        tokenLocal = response.extract().body().jsonPath().get("apitoken").toString();
        userLocal = "userlocal";

        LOGGER.info("Created user with rolegroup LOCAL_ADMIN and saved the token");
    }

    @After
    @Transactional
    public void reset(){
        LOGGER.info("Resetting FLYWAY...");
        FLYWAY.clean();
        response = null;
        request = null;
    }


    @Given("^a REST client unauthenticated$")
    public void givenRestClientUnauthenticated() {
        request = with().port(port).config(restAssuredConfig).contentType("application/json").auth().none();
        LOGGER.info("Using "+RestAssured.class.getName()+" unauthenticated");
    }

    @Given("^a REST client authenticated as (.*) with password (.*)$")
    public void givenRestClientAuthenticated(String login, String password) {
        request = with().port(port).config(restAssuredConfig).contentType("application/json").auth().preemptive().basic(login, password);
        LOGGER.info("Using "+RestAssured.class.getName()+" authenticated");
    }

    @Given("^a REST client authenticated with token and role LOCAL_ADMIN$")
    public void givenRestClientAuthenticatedWithToken() {
        request = with().port(port).config(restAssuredConfig).contentType("application/json").auth().preemptive().basic(userLocal, tokenLocal);
        LOGGER.info("Using "+RestAssured.class.getName()+" authenticated with token");
    }

    @When("^request json body has:$")
    public void requestJsonBodyHas(Map<String, String> jsonComponents) throws Throwable {
        if (!jsonComponents.isEmpty() && !jsonComponents.keySet().contains("")) {
            final Map<String, Object> jsonComponentsProcessed = new HashMap<>();
            jsonComponents.entrySet().stream().forEach(entry -> {
                String oldValue = entry.getValue();
                if (oldValue.contains("[")) {
                    String[] arrayOfValues = oldValue.replaceAll("\\[|\\]| ", "").split(",");
                    for (int x = 0; x < arrayOfValues.length; x++) {
                        arrayOfValues[x] = processFullUrl(arrayOfValues[x]);
                    }
                    jsonComponentsProcessed.put(entry.getKey(), arrayOfValues);
                } else {
                    oldValue = processFullUrl(oldValue);
                    jsonComponentsProcessed.put(entry.getKey(), oldValue);
                }
            });
            String json = jsonParser.toJson(jsonComponentsProcessed);
            System.out.println(json);
            request.body(json);
        }
    }

    @When("^request uri-list body has:$")
    public void requestUriListBodyHas(List<String> uriList) {
        request.contentType("text/uri-list");
        if (!uriList.isEmpty()) {
            String body = uriList.stream().map(this::processFullUrl)
                    .collect(Collectors.joining("\n"));
            request.body(body);
        }
    }

    @When("^request body is (.*)")
    public void requesthBodyIs(String body) {
        if (body !=null && !"".equals(body)) {
            request.body(body);
        }
    }

    @And("^send (.+) (.+)$")
    public void sendMethodPath(String method, String path) {
        URI fullUrl = URI.create(processFullUrl(path));
        System.out.println(fullUrl.toString());
        switch (method) {
            case "GET":
                response = request.get(fullUrl).then();
                break;
            case "POST":
                final String fullUrlStr="http://127.0.0.1:" + port + path;
                response = request.post(URI.create(fullUrlStr)).then();
                break;
            case "PUT":
                response = request.put(fullUrl).then();
                break;
            case "PATCH":
                response = request.patch(fullUrl).then();
                break;
            case "DELETE":
                response = request.delete(fullUrl).then();
                break;
            default:
                break;
        }
        System.out.println(response.extract().body().jsonPath().get().toString());
    }

    @Then("^the response status is (\\d+)$")
    public void ThenTheResponseStatusIs(int status) {
        response.statusCode(status);
    }

    @And("^property (.*) contains (.*)$")
    public void andPropertyContains(String property, String value) {
        if (property!=null && !"".equals(property)) {
            response.body(property, hasToString(value));
        }
    }

    @Then("^the response search at '(.+)' has items:$")
    public void theResponseSearchAtHasItems(String expression, List<String> items) {
        response.body(expression, hasItem(items));
    }

    @Then("^the response search at '(.+)' equal to (.+)$")
    public void theResponseSearchAtEqualTo(String expression, String match) {
        response.body(expression, equalTo(match));
    }

    private String processFullUrl(String data) {
        String key = "=";
        String exclude = "?";
        if (data.contains(key) && !data.contains(exclude)) {
            int indexOf = data.indexOf(key);
            String entityClass = data.substring(0, indexOf);
            String result = "http://127.0.0.1:" + port + "/" + entityClass.toLowerCase() + "/" + getIdFromData(data, indexOf);

            LOGGER.warn("accessing " + result);
            return result;
        }
        return data;
    }

    private String getIdFromData(String dataWithTypeAndName, int keyPos) {
        String id = "0";
        String entityClass = dataWithTypeAndName.substring(0, keyPos);
        String entityName = dataWithTypeAndName.substring(keyPos + 1, dataWithTypeAndName.length());
        String fieldName = entityClass.equals(Account.class.getSimpleName()) ? "username" : "name";
        String attrName = "e";
        if (entityClass.equals(VirtualhostGroup.class.getSimpleName())) {
            entityClass = VirtualHost.class.getSimpleName();
            attrName = "e.virtualhostgroup";
        }

        String jpqlFindByName ="SELECT " + attrName + " FROM " + entityClass + " e WHERE e." + fieldName + " = '" + entityName + "'";
        Query query = em.createQuery(jpqlFindByName);
        AbstractEntity entity = null;

        try {
            entity = (AbstractEntity) query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warn("CUCUMBER: " + dataWithTypeAndName + " NOT FOUND (" + e.getMessage() + ")");
        } finally {
            if (entity != null) {
                id = String.valueOf(entity.getId());
            } else {
                LOGGER.warn("CUCUMBER: " + dataWithTypeAndName + " NOT FOUND");
            }
        }
        return id;
    }



}
