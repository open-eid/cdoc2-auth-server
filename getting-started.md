## Running locally (localhost)

This file describes how to run `cdoc2-auth-server` in your local development machine, without
external infrastructure.

### Prerequisites
cdoc2-auth-token is not published to Maven Central. Build and install it first:

```bash
git clone https://github.com/open-eid/cdoc2-auth.git
cd cdoc2-auth && mvn clean install -DskipTests
```

### Installing and creating PostgreSQL DB in Docker

#### Install PostgreSQL in Docker
(Docker must be installed)

From `db-changelog` directory run:
```bash
cd db-changelog
docker compose up -d
```
This starts a Postgres container (`db-auth`, see `db-changelog/docker-compose.yml`) listening on
`localhost:7433`.

#### Create DB
From `db-changelog` directory run:
```bash
mvn clean compile liquibase:update
```

### Compiling the servers
From `cdoc2-auth-server` (repo root) directory run:
```bash
mvn clean install
```

### Running
From `cdoc2-rp-server` (repo root) directory
(psql in docker must be running)

The bundled `webapp/src/main/resources/application.properties` deliberately avoids configuring
the mandatory cryptographic keys used by the application. The `webapp/src/test/resources` folder
provides sample keys that can be used for local execution.
Provide a custom `application.properties` in the same folder as the jar or the `java -jar` 
command if you need to override any property (see README.md for the full list of `app.*` /
`spring.*` properties).

To use the sample test keys:

```bash
cp webapp/src/test/resources/ec*.pem .
cat > application.properties<< EOF
app.jwt.ecPrivateKeyPem=ec-es256-private.pem
app.well-known.publicKeys=ec-key-2025.pem,ec-key-2026.pem
app.well-known.activePublicKey=ec-key-2026.pem
EOF

```

Run the app:

```bash
java -jar webapp/target/cdoc2-auth-server-webapp-VER.jar
```
where VER is the version of the package built by `mvn install` previously (e.g.
`target/cdoc2-auth-server-webapp-0.8.0.jar`).

Run the server with `-Dlogging.config=target/test-classes/logback.xml` if you need to see logs.

Note: to enable TLS handshake debugging, add `-Djavax.net.debug=ssl:handshake` option.

The logging format can be changed by providing logback configuration.
An example OpenTelemetry-compatible Logback configuration is included in `otel-logback.xml`.
To include the logback configuration, use the `-Dlogging.config` JVM option.

Example of running the server with `otel-logback.xml`:
```
java -Dlogging.config=webapp/src/main/resources/otel-logback.xml -jar webapp/target/cdoc2-auth-server-webapp-VER.jar
```

By default, the server listens on `https://localhost:7500` and its actuator (management) endpoints
on `https://localhost:17500`.

When building the session token, `cdoc2-auth-server` fetches a session nonce from one of the
mediation servers configured in `app.session-nonce.uris` (defaults to `cdoc2-rp-server` on
`localhost:7600`, plus `cdoc2-capsule-server` on `8443`/`8442`). At least one of those needs to be
running locally for `/auth/start` to succeed end-to-end — see `cdoc2-rp-server/getting-started.md`.

#Testing
### Check that the server is up
```bash
curl -k https://localhost:17500/actuator/health
```
Response:
```json
{"status":"UP","components":{"db":{"status":"UP","details":{"database":"PostgreSQL","validationQuery":"isValid()"}},"livenessState":{"status":"UP"},"readinessState":{"status":"UP"}}}
```
```bash
curl -k https://localhost:17500/actuator/info
```

### Fetch signing keys
```bash
curl -k https://localhost:7500/.well-known/jwks.jws
```
Returns the public key(s) (`app.well-known.publicKeys`) that `cdoc2-auth-server` uses to sign
session tokens.

### Start an authentication process
Uses the SK demo Mobile-ID test number/identifier (requires `cdoc2-rp-server` or another
`app.session-nonce.uris` target running, and network access to `tsp.demo.sk.ee`):
```bash
curl -i -k -X POST https://localhost:7500/auth/start \
-H 'Content-Type: application/json' \
-H 'Accept: application/json' \
-d '{"identifier":"etsi/PNOEE-51307149560","mobileNr":"+37269930366"}'
```
Response:
```
HTTP/1.1 201
Location: /auth/status/9a7c3717d21f5cf19d18fa4fa5adee21
{"vc":"5702"}
```
