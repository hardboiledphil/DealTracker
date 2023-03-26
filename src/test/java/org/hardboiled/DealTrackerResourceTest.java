package org.hardboiled;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@QuarkusTest
public class DealTrackerResourceTest {

    @Test
    public void testCreate() {

        val dt1 = DealTracker.builder()
                .dealReference("DT1-1")
                .chain("Chain1")
                .chainNumber(123)
                .arrivalTime(LocalDateTime.now())
                .build();

        given().body(dt1).contentType("application/json")
                .when().post("/dealtracker/process")
                .then().statusCode(204);

        given()
                .when().get("/dealtracker/get/" + dt1.dealReference)
                .then().statusCode(200)
                .body("arrivalTime", notNullValue())
                .body("sentTime", blankOrNullString())
                .body("vestCompleteTime", blankOrNullString())
                .body("appCompleteTime", blankOrNullString());

        given()
                .when().delete("/dealtracker/delete/" + dt1.dealReference)
                .then().statusCode(204);
    }

    @Test
    public void testCreateAndUpdate() {

        val dt2 = DealTracker.builder()
                .dealReference("DT2-1")
                .chain("Chain1")
                .chainNumber(123)
                .arrivalTime(LocalDateTime.now())
                .build();

        // send the first message in and assert it's processed
        given().body(dt2).contentType("application/json")
                .when().post("/dealtracker/process")
                .then().statusCode(204);

        // get the first message back, assert values, retain object so we have message populated with id
        var dt2Updated = given()
                .when().get("/dealtracker/get/" + dt2.dealReference)
                .then().statusCode(200)
                .body("arrivalTime", notNullValue())
                .body("sentTime", blankOrNullString())
                .body("vestCompleteTime", blankOrNullString())
                .body("appCompleteTime", blankOrNullString())
                .extract().body().as(DealTracker.class);

        // assert that nothing is inProcessing
        given()
                .when().get("/dealtracker/getDealsInProcessing")
                        .then().statusCode(200)
                        .body("", Matchers.hasSize(0));
        // assert that first message is waiting
        given()
                .when().get("/dealtracker/getDealsWaiting")
                .then().statusCode(200)
                .body("", Matchers.hasSize(1));

        dt2Updated.setSentTime(LocalDateTime.now());
        given().body(dt2Updated).contentType("application/json")
                .when().post("/dealtracker/process")
                .then().statusCode(204);

        dt2Updated = given()
                .when().get("/dealtracker/get/" + dt2.dealReference)
                .then().statusCode(200)
                .body("arrivalTime", notNullValue())
                .body("sentTime", notNullValue())
                .body("vestCompleteTime", blankOrNullString())
                .body("appCompleteTime", blankOrNullString())
                .extract().body().as(DealTracker.class);

        // assert that second message is inProcessing
        given()
                .when().get("/dealtracker/getDealsInProcessing")
                .then().statusCode(200)
                .body("", Matchers.hasSize(1));
        // assert that second message is not waiting
        given()
                .when().get("/dealtracker/getDealsWaiting")
                .then().statusCode(200)
                .body("", Matchers.hasSize(0));

        given()
                .when().delete("/dealtracker/delete/" + dt2.dealReference)
                .then().statusCode(204);
    }
}
