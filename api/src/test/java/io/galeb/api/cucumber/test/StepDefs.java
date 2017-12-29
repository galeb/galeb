package io.galeb.api.cucumber.test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RedirectConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import io.galeb.api.Application;
import io.galeb.api.security.LocalAdmin;
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

import javax.annotation.PostConstruct;
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
@PropertySource("classpath:application.properties")
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

    private static final Gson jsonParser = new GsonBuilder().setPrettyPrinting().create();

    private RequestSpecification request;
    private ValidatableResponse response;
    private String token;

    private RedirectConfig redirectConfig = RestAssuredConfig.config().getRedirectConfig().followRedirects(false);
    private RestAssuredConfig restAssuredConfig = RestAssuredConfig.config().redirect(redirectConfig);


    @PostConstruct
    public void init() {
        System.out.println("dbUrl: " + dbUrl);
        FLYWAY.setDataSource(dbUrl, dbUsername, dbPassword);
        FLYWAY.migrate();
    }

    @Transactional
    public void ddeleteAll() {
        Stream.of(
                Account.class, BalancePolicy.class, Environment.class, HealthCheck.class, HealthStatus.class,
                Pool.class, Project.class, Role.class, RoleGroup.class, Rule.class, RuleOrdered.class,
                Target.class, Team.class, VirtualhostGroup.class, VirtualHost.class
        ).forEach(c -> {
            em.joinTransaction();
            Query query = em.createQuery("DELETE FROM " + c.getSimpleName());
            query.executeUpdate();
        });
    }


    @Transactional
    @Given("^reset")
    public void reset(){
        ddeleteAll();
        response = null;
        request = null;
    }


    @Given("^a REST client unauthenticated$")
    public void givenRestClientUnauthenticated() throws Throwable {
        request = with().config(restAssuredConfig).contentType("application/json").auth().none();
        LOGGER.info("Using "+RestAssured.class.getName()+" unauthenticated");
    }

    @Given("^a REST client authenticated as (.*) with password (.*)$")
    public void givenRestClientAuthenticated(String login, String password) {
        request = with().config(restAssuredConfig).contentType("application/json").auth().preemptive().basic(login, password);
        LOGGER.info("Using "+RestAssured.class.getName()+" authenticated");
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
            request.body(json);
        }
    }

    @When("^request uri-list body has:$")
    public void requestUriListBodyHas(List<String> uriList) throws Throwable {
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
    public void sendMethodPath(String method, String path) throws Throwable {
        URI fullUrl = URI.create(processFullUrl(path));
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
    }

    @Then("^the response status is (\\d+)$")
    public void ThenTheResponseStatusIs(int status) throws Throwable {
        response.statusCode(status);
    }

    @And("^property (.*) contains (.*)$")
    public void andPropertyContains(String property, String value) throws Throwable {
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
            return "http://localhost/" + entityClass.toLowerCase() + "/" + getIdFromData(data, indexOf);
        }
        return data;
    }

    private String getIdFromData(String dataWithTypeAndName, int keyPos) {
        String id = "0";
        String entityClass = dataWithTypeAndName.substring(0, keyPos);
        String entityName = dataWithTypeAndName.substring(keyPos + 1, dataWithTypeAndName.length());
        String jpqlFindByName ="SELECT e FROM " + entityClass + " e WHERE e.name = '" + entityName + "'";
        Query query = em.createQuery(jpqlFindByName);
        AbstractEntity entity = null;

        try {
            entity = (AbstractEntity) query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warn("CUCUMBER: " + dataWithTypeAndName + "NOT FOUND (" + e.getMessage() + ")");
        } finally {
            if (entity != null) {
                id = String.valueOf(entity.getId());
            } else {
                LOGGER.warn("CUCUMBER: " + dataWithTypeAndName + "NOT FOUND");
            }
        }
        return id;
    }



}
