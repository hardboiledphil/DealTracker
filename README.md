# deal-tracker

Spike to show we can store the state of deal flows as "messages" arrive into the system.
We are required to be able to say how many messages are "waiting" to be sent to downstream
systems and how many have been sent but not completed and hence "in progress".

In the end the application derived from this work was deployed into production where it
handled a flow of Kafka sourced events to drive the "LIVE" state which can be queried
very efficiently.  I think however there was some weird issue with H2 and I ended up
just replacing it with a hashmap/set as without a properly persisted database the state
could not be guaranteed but was however more of a monitoring tool than a business critical
application.

to add a dealTracker entity

curl -d '{"dealReference":"abc123::1","chain":"chainABC","chainNumber":1,"arrivalTime":"2024-09-04T05:06:00","sentTime":"2024-09-05T06:07:00","vestCompleteTime":null,"appCompleteTime":null}' -H 'Content-Type: application/json' -X POST http://localhost:8080/dealtracker/process

Note if you populate all 4 date fields then it will consider it completed and ignore/delete it

To query:

curl http://localhost:8080/dealtracker/getDealsInProcessing
curl http://localhost:8080/dealtracker/getDealsWaiting
curl http://localhost:8080/dealtracker/getAll
curl http://localhost:8080/dealtracker/get/transactionRef/abc123::2



## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/deal-tracker-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- REST resources for Hibernate ORM with Panache ([guide](https://quarkus.io/guides/rest-data-panache)): Generate JAX-RS resources for your Hibernate Panache entities and repositories
- JDBC Driver - H2 ([guide](https://quarkus.io/guides/datasource)): Connect to the H2 database via JDBC
- RESTEasy Classic ([guide](https://quarkus.io/guides/resteasy)): REST endpoint framework implementing JAX-RS and more

## Provided Code

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
