# gateway-service (LogSpectra sample)

Minimal Spring Boot app that uses **logspectra-starter** to ship structured JSON logs to Kafka. **logspectra-backend** consumes `service-logs`, indexes into Elasticsearch, and **logspectra-ui** displays them.

## Flow

```
gateway-service (this app) → Kafka topic service-logs → logspectra-backend → Elasticsearch → logspectra-ui
```

## Prerequisites

- JDK 21, Maven 3.9+
- LogSpectra stack running locally (from `logspectra-backend`):

```bash
cd ../logspectra-backend
docker compose up -d postgres redis zookeeper kafka elasticsearch
mvn spring-boot:run
```

- UI (optional): `cd ../logspectra-ui && pnpm dev` (or docker compose)

## Install the starter locally

The dependency matches your published coordinates. For local dev, install the sibling starter into `~/.m2`:

```bash
cd ../logspectra-starter
mvn clean install -DskipTests
mvn install:install-file \
  -Dfile=target/logspectra-starter-1.0.2.jar \
  -DgroupId=com.github.kmvipin \
  -DartifactId=logspectra-starter \
  -Dversion=v1.0.1 \
  -Dpackaging=jar
```

(Adjust the `-Dfile=` version if your starter `pom.xml` version differs.)

## Configure project ID

1. Sign in to LogSpectra UI and create or open a project.
2. Copy the project **UUID** from the UI or API.
3. Set it when running:

```bash
export LOGSPECTRA_PROJECT_ID="<your-project-uuid>"
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
mvn spring-boot:run
```

Or edit `application.yml` and replace `YOUR_PROJECT_ID`.

`logspectra.project-id` in Kafka JSON must match the project you view in the UI.

## Run

```bash
mvn spring-boot:run
```

## Generate logs

```bash
curl "http://localhost:8082/api/hello?name=logspectra"
curl "http://localhost:8082/api/error-demo"   # expect HTTP 500 + error log in UI
curl "http://localhost:8082/api/error-burst?count=20"   # 20 ERROR lines to Kafka (HTTP 200)
curl "http://localhost:8082/api/checkout?cartItems=0" # checkout bug demo (502 + stack trace in LogSpectra)
```

With `gateway.demo-logs.enabled=true`, the app also emits **3 ERROR logs per second** automatically (`errors-per-tick`). Half of the checkout errors hit an **intentional empty-cart bug** in `CheckoutUpstreamService` (`10 / cartItemCount`) so LogSpectra **Debug** + **remediation PR** can patch `src/main/java/.../CheckoutUpstreamService.java` on GitHub.

### LogSpectra remediation demo

1. Push this repo to `LOGSPECTRA_GIT_URL` (default `https://github.com/logspectra/gateway-service.git`).
2. Enable GitHub App on the backend (`SETUP-GITHUB-APP.md` in logspectra-backend).
3. In UI: Log Explorer → `gateway-service` ERROR → enable **Open GitHub remediation PR** → **Debug**.
4. Expected fix: guard `cartItemCount == 0` before retry-budget math; backend uses one branch per issue (`logspectra/fix-gateway-service-<fp>`) — repeat Debug updates that branch and reuses the open PR.

In the UI, filter by service **gateway-service** and your project.

## Dependency (reference)

```xml
<dependency>
  <groupId>com.github.kmvipin</groupId>
  <artifactId>logspectra-starter</artifactId>
  <version>v1.0.1</version>
</dependency>
```
