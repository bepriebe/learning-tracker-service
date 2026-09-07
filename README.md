# Learning Tracker Service

A small Spring Boot backend for recording learning goals and learning logs. The
service is also used as a practical foundation for container, delivery and
operations exercises.

## Current Features

- Create, list, retrieve and update goals.
- Move goals through the states `TODO`, `IN_PROGRESS` and `DONE`.
- Reject invalid goal status transitions with `409 Conflict`.
- Create and list learning logs.
- Optionally link a learning log to an existing goal.
- Validate request data and return structured error responses.
- Persist runtime data in PostgreSQL.
- Run automated API and application tests against an in-memory H2 database.

## Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA and Hibernate
- Maven Wrapper
- PostgreSQL 18 for the running application
- H2 for automated tests
- Docker and Docker Compose
- pgAdmin as an optional local database tool

Java, Maven and the databases run in containers. The host only needs Docker
with the Compose plugin for the documented workflow.

## API

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/goals` | Create a goal |
| `GET` | `/goals` | List all goals |
| `GET` | `/goals/{id}` | Retrieve one goal |
| `PUT` | `/goals/{id}` | Update title and description |
| `POST` | `/goals/{id}/start` | Move a goal from `TODO` to `IN_PROGRESS` |
| `POST` | `/goals/{id}/complete` | Move a goal from `IN_PROGRESS` to `DONE` |
| `POST` | `/learning-logs` | Create a learning log |
| `GET` | `/learning-logs` | List all learning logs |

The allowed goal lifecycle is:

```text
TODO -> IN_PROGRESS -> DONE
```

Trying to complete a `TODO` goal or restart a `DONE` goal returns
`409 Conflict`. Unknown goal IDs return `404 Not Found`. Invalid request data
returns `400 Bad Request` with field-specific validation messages.

## Run Locally

Build and start the application with PostgreSQL:

```bash
docker compose up --build -d app
```

Check the application health:

```bash
curl http://localhost:8080/actuator/health
```

Create a goal:

```bash
curl -i \
  -H "Content-Type: application/json" \
  -d '{"title":"Build a delivery pipeline","description":"Automate tests and image builds."}' \
  http://localhost:8080/goals
```

List goals:

```bash
curl http://localhost:8080/goals
```

Create a learning log without a goal:

```bash
curl -i \
  -H "Content-Type: application/json" \
  -d '{"topic":"Docker Compose","summary":"Practised service dependencies and health checks.","nextStep":"Add automated delivery."}' \
  http://localhost:8080/learning-logs
```

To link a learning log to a goal, add its UUID as `goalId` to the JSON body.

Stop and remove the containers and Compose network:

```bash
docker compose down
```

The PostgreSQL data remains in its named volume and is reused on the next
start.

## Optional pgAdmin

Start the application and pgAdmin with the `tools` profile:

```bash
docker compose --profile tools up --build -d app pgadmin
```

Open `http://localhost:5050` and sign in with the local development account:

```text
Email:    admin@example.com
Password: local-development
```

Register PostgreSQL in pgAdmin with these connection values:

```text
Host:                 postgres
Port:                 5432
Maintenance database: learning_tracker
Username:             learning_tracker
Password:             learning_tracker
```

PostgreSQL is intentionally not published on a host port. The application and
pgAdmin reach it through the internal Compose network as `postgres:5432`.
All credentials shown here are local development defaults and must not be used
for a production deployment.

## Tests

Run the test suite in a temporary Java 21 container:

```bash
docker compose --profile test run --rm test
```

The Maven wrapper, project descriptor and source directories are mounted
read-only. Named volumes cache Maven dependencies and keep generated `target`
files outside the host workspace. The tests use H2 in memory and do not require
the PostgreSQL service. The current suite contains 19 application and API tests.

The multi-stage Dockerfile also provides a test target for explicit image-build
checks:

```bash
docker build --target test .
```

## Configuration

The application reads its database connection from environment variables:

| Variable | Local default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/learning_tracker` |
| `DB_USERNAME` | `learning_tracker` |
| `DB_PASSWORD` | `learning_tracker` |

Docker Compose supplies container-specific values and changes the database host
to the Compose service name `postgres`.

## Persistent Volumes

The Compose project uses four named volumes:

| Volume | Contents |
| --- | --- |
| `postgres-data` | PostgreSQL database files |
| `pgadmin-data` | pgAdmin configuration |
| `maven-cache` | Downloaded Maven dependencies |
| `test-target` | Generated test and build output |

For normal shutdown, use `docker compose down`. The following command performs
a full local reset and deletes all four volumes, including the PostgreSQL data:

```bash
docker compose down -v
```

Use it only when all local application and tool data may be discarded.

## Current Limitations

- Hibernate currently updates the runtime schema through `ddl-auto=update`;
  explicit database migrations are not implemented yet.
- Development and production configurations are not separated yet.
- Image publication, deployment automation and orchestration are not part of
  the current `main` branch.
- Authentication and authorization are not implemented.
- Monitoring currently consists of the Spring Boot health endpoint.
