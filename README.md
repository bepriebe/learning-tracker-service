# Learning Tracker Service

This service is the first runnable backend for Project 1.

## Stack

- Java 21
- Spring Boot
- Maven Wrapper inside the build container
- PostgreSQL in the runtime Compose stack
- H2 only for fast tests inside the test container

## Container Workflow

Build and run the test image:

```bash
docker compose --profile test build test
```

The test build runs `./mvnw test` in a Java 21 build container. It uses H2 as
an in-memory database for the initial Spring context test, so the fast test path
does not need PostgreSQL yet.

Start the service and PostgreSQL:

```bash
docker compose up --build -d app
```

Check the first operational endpoint:

```bash
curl http://localhost:8080/actuator/health
```

Create the first goal:

```bash
curl -i \
  -H "Content-Type: application/json" \
  -d '{"title":"Build the first API slice","description":"Start with goals."}' \
  http://localhost:8080/goals
```

List goals:

```bash
curl http://localhost:8080/goals
```

Stop the stack:

```bash
docker compose down
```

The host only needs Docker for this project workflow. Java and Maven run inside
the build image. The application container receives `DB_URL`, `DB_USERNAME` and
`DB_PASSWORD` from Compose and reaches PostgreSQL through the Compose service
name `postgres`.

The PostgreSQL volume survives `docker compose down`. Remove it deliberately
with `docker compose down -v` when the local runtime data should be discarded.

## Next Slice

Version 0.1 starts with goals and learning sessions. Goals now have the first
API contract and validation slice. The next code slice should decide whether to
add goal lookup or start learning sessions.
