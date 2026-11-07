# api-gateway

> Single entry point of TicketFlow: clients talk to the gateway, never to the services directly.

## Concept illustrated
Spring Cloud Gateway (WebFlux flavour) as a reverse proxy. A **route** =
an id + a target `uri` + **predicates** (when does it match?) + **filters**
(what do we change on the way?).

The explicit customer route uses Eureka and Spring Cloud LoadBalancer to
resolve its destination. The optional discovery locator is enabled and
restricted to `customer-service`.

## Overview
- **Port:** 8080
- **Depends on:** `customer-service` (8081)
- **Runtime:** WebFlux on Netty (not Tomcat)

| Route | Predicate | Target |
|---|---|---|
| `customer-service` | `Path=/api/customers/**` | `lb://customer-service` |

Filter used: `AddResponseHeader=X-Served-By, api-gateway`.

## Run locally
Terminal 1:

    ./mvnw -pl services/discovery-server spring-boot:run

Terminal 2:

    ./mvnw -pl services/customer-service spring-boot:run

Terminal 3:

    ./mvnw -pl services/api-gateway spring-boot:run

Call customer-service **through the gateway** (port 8080, not 8081):

    curl -i -X POST localhost:8080/api/customers \
      -H 'Content-Type: application/json' \
      -d '{"firstName":"Sara","lastName":"Idrissi","email":"sara@example.com"}'

    curl -i localhost:8080/api/customers

Look for the `X-Served-By: api-gateway` response header. List the routes the
gateway knows about:

    curl localhost:8080/actuator/gateway/routes

The explicit route keeps `/api/customers`. The locator also generates a route
under `/customer-service/**`; for example:

    curl -i localhost:8080/customer-service/api/customers

The locator strips the `/customer-service` prefix before forwarding. Its
generated route does not add the explicit route's `X-Served-By` header.

## Run the tests
    ./mvnw -pl services/api-gateway test

The test starts the gateway and a stub backend, so `customer-service` does not need to run.

## Exercise
1. Add a second route for `/api/events/**` pointing to `http://localhost:8082`.
   What does the gateway answer while nothing listens on 8082?
2. Start **two** instances of customer-service (the second one with
   `--server.port=8083`) and observe how Eureka and LoadBalancer handle them.
3. Add a `StripPrefix=1` filter and see how the path received by the backend changes.

## Common pitfalls
- Spring Cloud 2025.1 renamed the properties: routes live under
  `spring.cloud.gateway.server.webflux.routes`. Old tutorials using
  `spring.cloud.gateway.routes` silently do nothing.
- Never add `spring-boot-starter-web` (Tomcat/MVC) next to the WebFlux gateway:
  the two models conflict and the application fails to start.
- A 404 from the gateway means no route matched; a 5xx (502/503) means a route
  matched but the backend is unreachable.
