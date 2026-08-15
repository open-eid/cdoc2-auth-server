# CDOC2 Auth Server

## Structure

- `adapter`
    - Implementation details for data access, input and output
    - May depend on: `app`, `openapi`
- `app`
    - Business logic, completely agnostic towards data access implementation. Internally structured
      according to logical application usecases. Defines interfaces for any needed external data
      access, which are then implemented in the `adapter` module
    - May not have dependencies to other modules
- `db-changelog`
    - Liquibase changes and related helpers
    - May not have dependencies to other modules
- `openapi`
    - Openapi definition and code generation of cdoc2-auth-server REST API
    - May not have dependencies to other modules
- `webapp`
    - Spring boot application
    - May depend on `adapter`, `db-changelog`

### Running from JAR

- Create database (see README.md under /db-changelog)
- `mvn clean install`. JAR is created under /webapp/target.
- run JAR - `java -jar webapp.jar`. Provide custom `application.properties` in same folder as needed

### Application properties

In configuration files, the following properties must start with the `app.` prefix:
`app.restclient.session-nonce.retries`

| application prop                                    | default                                                | description                                                                                                                                                                                        |
|:----------------------------------------------------|:-------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| session-nonce.uris                                  |                                                        | comma-seprated list of URI-s that are queried for session nonces when composing the session token                                                                                                  |
| restclient.session-nonce.retries                    | 3                                                      | number of retries when session nonce request fails                                                                                                                                                 |
| restclient.session-nonce.read-timeout               | 5000                                                   | read timeout for session nonce requests, in millisecond                                                                                                                                            |
| restclient.session-nonce.connection-request-timeout | 5000                                                   | connection timeout for session nonce requests, in millisecond                                                                                                                                      |
| jwt.ecPrivateKeyPem                                 |                                                        | PEM-encoded resource for the EC ES256 private key to use for signing the session token                                                                                                             |
| well-known.publicKeys                               |                                                        | List of PEM-encoded resources for the public key(s) advertised by the /.well-known/jwks.jws endpoint                                                                                               |
| well-known.activePublicKey                          |                                                        | Name of the public key that corresponds to `jwt.ecPrivateKeyPem`. <br/>Must be contained in `well-known.publicKeys` and is used to derive the `kid` value for the JWT header                       |
| rp.sid.name                                         |                                                        | Relying party name that auth-server presents to the SID services                                                                                                                                   |
| rp.sid.uuid                                         |                                                        | Relying party UUID that auth-server presents to the SID services                                                                                                                                   |
| rp.mid.name                                         |                                                        | Relying party name that auth-server presents to the MID services                                                                                                                                   |
| rp.mid.uuid                                         |                                                        | Relying party UUID that auth-server presents to the MID services                                                                                                                                   |
| rp.certificate-level                                | QUALIFIED                                              | The required certificate level when authenticating through SID/MID services                                                                                                                        |
| rp.scheme-name                                      | smart-id-demo                                          | Name of the SID scheme used (eg. `smart-id`)                                                                                                                                                       |
| smartid.client.hostUrl                              |                                                        | URL of the SID RP API                                                                                                                                                                              |
| smartid.client.statusPollTimeoutSeconds             | 1                                                      | Max time the SID API may hold a session status request open (server-side long poll) before responding with the current status, in seconds. Value of 0 means request will return only when complete |
| mobileid.client.hostUrl                             |                                                        | URL of the MID RP API                                                                                                                                                                              |
| mobileid.client.timeoutSeconds                      | 5                                                      | Timeout for MID connections, in seconds                                                                                                                                                            |
| cleanup.rate                                        | 30000                                                  | milliseconds between auth process cleanup job executions                                                                                                                                           |
| cleanup.authProcessMaxAgeMinutes                    | 5                                                      | maximum allowable age for an auth process in minutes                                                                                                                                               |
| cleanup.authProcessDeletionLimit                    | 1000                                                   | limit to the number of records deleted by a single run of the cleanup job                                                                                                                          |
| app.auth.display-text.et                            | `Please confirm authentication: {semanticsIdentifier}` | Display text shown to the user in Estonian during MID/SID authentication prompts. Supports the `{semanticsIdentifier}` placeholder, which is replaced with users ID.                               |
| app.auth.display-text.en                            | `Please confirm authentication: {semanticsIdentifier}` | Display text shown to the user in English during MID/SID authentication prompts. Supports the `{semanticsIdentifier}` placeholder, which is replaced with users ID.                                |
| app.auth.display-text.ru                            | `Please confirm authentication: {semanticsIdentifier}` | Display text shown to the user in Russian during MID/SID authentication prompts. Supports the `{semanticsIdentifier}` placeholder, which is replaced with users ID.                                |
| app.auth.display-text.lt                            | `Please confirm authentication: {semanticsIdentifier}` | Display text shown to the user in Lithuanian during MID/SID authentication prompts. Supports the `{semanticsIdentifier}` placeholder, which is replaced with users ID.                             |
| app.auth.display-text.defaultLanguage               | et                                                     | Language to use when the client does not specify a language. Accepted values: `et`, `en`, `ru`, `lt`.                                                                                              |
| app.session-token.issuer                            |                                                        | The issuer added to the created session token.                                                                                                                                                     |

### Spring properties

In configuration files, the following properties must start with the `spring.` prefix:
`spring.datasource.url`

| spring prop                  | description |
|:-----------------------------|:------------|
| datasource.url               |             | 
| datasource.username          |             | 
| datasource.password          |             | 
| datasource.driver-class-name |             | 

### SSL Bundles

Keystores and trust stores are defined with Spring SSL bundles.

Trust store example, where `somebundle` is a placeholder for an actual bundle name:

```
spring.ssl.bundle.jks.somebundle.truststore.location=truststore.jks
spring.ssl.bundle.jks.somebundle.truststore.password=changeit
spring.ssl.bundle.jks.somebundle.truststore.type=jks
```

Keystore example, where `somebundle` is a placeholder for an actual bundle name::

```
spring.ssl.bundle.jks.somebundle.keystore.location=keystore.p12
spring.ssl.bundle.jks.somebundle.keystore.password=changeit
spring.ssl.bundle.jks.somebundle.keystore.type=pkcs12
spring.ssl.bundle.jks.somebundle.key.alias=authServerKey
```

Defined bundles:

| bundle name   | type                 | description                                                                    |
|:--------------|:---------------------|:-------------------------------------------------------------------------------|
| server-bundle | keystore, truststore | keystore and truststore (if any) to use for embedded server SSL connections    |
| sid-server    | truststore           | provides truststore for SID server connections                                 |
| mid-server    | truststore           | provides truststore for MID server connections                                 |
| trusted-infra | truststore           | provides truststore for REST clients communicating with other CDOC2 components |

### Building the docker image locally

To build Docker images:

```bash
./build-images.sh
```

To run the build container:

```bash
docker run --rm --network=host ghcr.io/open-eid/cdoc2-auth-server:0.7.0
```

### Key generation for JWT (session token) signing

```
openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out ec_keypair.pem \
  && openssl pkey -in ec_keypair.pem -pubout -out ec_public.pem
  
openssl ec -in ec_keypair.pem -out ec_private.pem
```

### Creating SBOM (Software Bill of Materials)

The SBOM report will be automatically generated at build time.

To manually create the SBOM report, run:

```
mvn cyclonedx:makeAggregateBom
```

The generated reports (`target/bom.json` and `target/bom.xml`) include dependencies from all
submodules.