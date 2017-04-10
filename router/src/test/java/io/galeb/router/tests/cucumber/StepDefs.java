
package io.galeb.router.tests.cucumber;

import javax.annotation.PostConstruct;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RedirectConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.galeb.router.tests.services.SimulatedBackendService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;

import static com.jayway.restassured.RestAssured.with;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Ignore
public class StepDefs {

    private static final Log LOGGER = LogFactory.getLog(StepDefs.class);

    private RedirectConfig redirectConfig = RestAssuredConfig.config().getRedirectConfig().followRedirects(false);
    private RestAssuredConfig restAssuredConfig = RestAssuredConfig.config().redirect(redirectConfig);
    private int backendPort = 8080;

    private RequestSpecification request;
    private ValidatableResponse response;

    @Autowired
    private SimulatedBackendService backendService;

    @PostConstruct
    public void init() {
        backendService.setBackendPort(backendPort).start();
    }

    @Before
    public void setUp() {
        response = null;
        request = null;
    }

    @After
    public void cleanUp() {

    }

    @Given("^a http client$")
    public void aHttpClient() throws Throwable {
        request = with().config(restAssuredConfig);
        LOGGER.info("Using "+RestAssured.class.getName());
    }

    @When("^send (.+) (.+)$")
    public void sendMethodPath(String method, String path) throws Throwable {
        URI fullUrl = URI.create(path);
        switch (method) {
        case "GET":
            response = request.get(fullUrl).then();
            break;
        case "POST":
            final String fullUrlStr="http://127.0.0.1:" + backendPort + path;
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

}
