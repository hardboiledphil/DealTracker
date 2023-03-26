package org.hardboiled;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testCreateEndpoint() {
        given()
          .when().get("/hello/fred")
          .then()
             .statusCode(200)
             .body(is("Hello RESTEasy fred"));
    }

}