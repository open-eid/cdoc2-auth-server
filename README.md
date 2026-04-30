# CDOC2 Auth Server

## Structure

- `adapter`
    - Implementation details for data access, input and output
    - May depend on: `app`, `openapi`
- `app`
    - Business logic, completely agnostic towards data access implementation.
      Internally structured according to logical application usecases. Defines interfaces
      for any needed external data access, which are then implemented in the `adapter` module
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
- run JAR - `java -jar webapp.jar`. Provide custom `application.properties` in same
  folder as needed

### Application properties

In configuration files, the following properties must start with the `app.` prefix:
`app.restclient.session-nonce.retries`

| application prop                                    | default       | description                                                                                       |
|:----------------------------------------------------|:--------------|:--------------------------------------------------------------------------------------------------|
| restclient.session-nonce.retries                    | 3             | number of retries when session nonce request fails                                                |
| restclient.session-nonce.read-timeout               | 5000          | read timeout for session nonce requests                                                           |
| restclient.session-nonce.connection-request-timeout | 5000          | connection timeout for session nonce requests                                                     |
| session-nonce.uris                                  |               | comma-seprated list of URI-s that are queried for session nonces when composing the session token |
| well-known.ec-private-key-name                      |               | name of the EC private key to use for signing the session token                                   |
| well-known.ec-key-kid                               |               | key id of the EC private key to use for signing the session token                                 |
| rp.name                                             |               | Relying party name that auth-server presents to the SID/MID services                              |
| rp.uuid                                             |               | Relying party UUID that auth-server presents to the SID/MID services                              |
| rp.certificate-level                                | QUALIFIED     | The required certificate level when authenticating through SID/MID services                       |
| rp.scheme-name                                      | smart-id-demo | Name of the SID scheme used (eg. `smart-id`)                                                      |
| smartid.client.hostUrl                              |               | URL of the SID RP API                                                                             |

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
| trusted-infra | truststore           | provides truststore for REST clients communicating with other CDOC2 components |
