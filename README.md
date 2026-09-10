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
- Jenkins declarative pipeline for continuous integration

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

## Jenkins Pipeline

The repository contains a declarative pipeline in `Jenkinsfile`:

| Stage | Action |
| --- | --- |
| `Prepare image tag` | Combines a branch hash, Git revision and build number |
| `Test` | Runs the test suite through the Compose `test` profile |
| `Build image` | Builds the Dockerfile `runtime` target |
| `Verify image` | Inspects the resulting local image |
| `Publish image` | Optionally pushes the verified image to GHCR |
| `Deploy dev` | On `dev` only: deploys the published image digest to k3s |

Each image is tagged as `learning-tracker-service:${IMAGE_TAG}`, where the tag
contains the first 12 characters of the branch SHA-256 hash, the abbreviated Git
revision and the build number. This distinguishes images from different branch
jobs. Concurrent
builds are disabled, console output includes timestamps and Jenkins retains the
ten most recent build records.

The `Test` stage retains its container until its Surefire reports have been
copied from `/workspace/target/surefire-reports/` into the Jenkins workspace at
`target/surefire-reports/`. Its `post { always { ... } }` block publishes
`TEST-*.xml` with the `junit` step, including when Maven reports failed tests.
The original test failure still fails the pipeline and prevents the image
stages from running. Missing reports are treated as an error, not an empty
successful test run.

Old workspace reports are deleted before testing. A Compose project name derived
from the job name, build number and workspace isolates each build's containers,
network and volumes, including across branch jobs. These Docker resources are
removed after publication, even if copying or publishing fails. This also means
the CI Maven cache starts fresh for each build; local Compose volumes are not
affected. Reports copied with `docker cp` belong to the agent user rather than
the container's root user.

The Jenkins agent needs a Linux shell, Docker with the Compose plugin and
permission to access the Docker daemon, plus `sha256sum` and `cut`. Jenkins needs
the JUnit plugin for the `junit` step. Java and Maven are still provided by the
project containers and do not need to be installed on the agent.

After pushing the branch, use **Build Now** in its Jenkins branch job. Open
**Test Result** on the finished build and confirm that all 19 tests passed;
subsequent builds provide test history. To check the failure path, temporarily
introduce a failing assertion on the CI branch and run another build: it must
fail, show the failed test, and skip both image stages. Revert that intentional
test change immediately afterward and run a successful build again. A webhook
for automatically building every push is not configured yet.

### Optional GHCR publication

The boolean build parameter `PUBLISH_IMAGE` defaults to false. On
`ci/jenkins-pipeline` and `main`, publication runs only when it is enabled.
On `dev`, publication runs automatically before deployment, regardless of the
parameter. Other branches still run CI without using the registry credential.

Create a Jenkins **Username with password** credential with ID `ghcr-push`:
username `bepriebe`, password a GitHub personal access token (classic) with
`write:packages`. Keep the existing GitHub discovery credential separate.
See [GitHub's registry authentication documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authenticating-to-the-container-registry).

Run the updated branch once with publication disabled so Jenkins registers the
parameter. Then use **Build with Parameters**, enable `PUBLISH_IMAGE`, and run
again. The `Publish image` stage logs the pushed image reference and Docker's
registry digest. The destination is
`ghcr.io/bepriebe/learning-tracker-service:${IMAGE_TAG}`.

The pipeline passes the token through standard input with shell tracing disabled.
Docker login configuration is stored in a temporary directory and removed on shell
exit. The image source label links the package to this GitHub repository.
New GHCR packages are private by default; a later Kubernetes deployment will need
pull credentials unless the package is deliberately made public.

### Kubernetes Dev deployment

Only the `dev` branch runs `Deploy dev`. The pipeline resolves the GHCR digest
after publication and passes it to `bash scripts/deploy-dev.sh`. The script
accepts only a SHA-256 digest from this application's GHCR repository; it does
not deploy a mutable tag or another repository's image.

Jenkins uses `/var/lib/jenkins/.kube/config`, context `learning-tracker-homelab`,
and identity `learning-tracker-deployer`. All deployment resources explicitly
target `learning-tracker-dev`. The agent needs Bash, kubectl, sed and awk in
addition to the CI tools. The one-time secret setup also uses OpenSSL.

Before the first Dev build, provision these two Secrets in `learning-tracker-dev`:

- `ghcr-pull`: type `kubernetes.io/dockerconfigjson`, with a GitHub classic token
  having `read:packages`. The application references this as an `imagePullSecret`.
- `learning-tracker-db`: key `password`, used by both PostgreSQL and the app.
  Run `bash scripts/create-dev-db-secret.sh` under an identity with the configured
  kube context. It generates a random password without printing it and leaves an
  existing Secret unchanged. Never commit generated credentials. Changing this
  Secret after database initialization does not change the password in PostgreSQL;
  rotation must be coordinated with the database.

`k8s/dev/postgres.yaml` defines an internal Service, a single-replica PostgreSQL
18 Deployment with `Recreate` strategy, and a 2 GiB PVC on the homelab's
`local-path` StorageClass. PostgreSQL 18 mounts `/var/lib/postgresql`, matching
the local Compose setup. This is a single-node-storage learning environment,
not an HA database or a backup solution. Do not delete the PVC to update the app.

`k8s/dev/app.yaml` defines the application's internal Service and Deployment.
The deployment script waits for PostgreSQL, substitutes the image digest using
`sed` without modifying the tracked file, applies the app and waits for rollout.
Startup and liveness use `/actuator/health/liveness`; readiness uses
`/actuator/health/readiness` and includes the database health indicator. Each
rollout has a five-minute timeout. A failure fails the Jenkins build; it does
not automatically roll back the deployment.

For a manual health check, on a machine with the kube context available:

```bash
kubectl --context=learning-tracker-homelab --namespace=learning-tracker-dev \
  port-forward service/learning-tracker-service 18080:8080
```

In a second terminal on that same machine:

```bash
curl --fail http://127.0.0.1:18080/actuator/health
curl --fail http://127.0.0.1:18080/goals
```

The service is currently accessible inside the cluster or through port-forward;
an Ingress is not configured. The planned mapping `main` to
`learning-tracker-staging` and release tags `v*` to `learning-tracker-prod`
with manual approval is not implemented yet. Tag discovery must be configured
in Jenkins before release-tag builds can be used.

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
- GHCR publication requires the Jenkins credential. Dev deployment additionally
  requires both Kubernetes Secrets and the configured k3s context.
- Staging and production deployments are not implemented yet.
- Authentication and authorization are not implemented.
- Monitoring currently consists of the Spring Boot health endpoint.
