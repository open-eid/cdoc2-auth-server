# Changelog

## [0.8.0]

### Improvements
* Language parameter for `auth/start` is constrained by openapi spec to be a nullable two character 
  string. Actual validation of the input value is moved to app api implementation. Unrecognized 
  language will result in HTTP 400 BAD REQUEST. Configured default language is applied only when 
  language is not specified in request. 

## [0.7.3]

### Improvements
* Dependency updates

### Fixes
* `/auth/status` returns `HTTP 404 NOT FOUND` for non-existent auth process instead of `HTTP 400`

## [0.7.2]

### Improvements
* Improve the tests coverage
* Small refactor to `startauth` usecase code to allow verification code creation testing.

## [0.7.1]

### Improvements
* Give more informative Internal Server Error results when auth/start/ encounters a problem 
  collecting session nonces from CDOC2 servers
* Use CycloneDX Maven plugin for SBOM creation

## [0.7.0]

### Improvements
* JWK for /.well-known/jwks.jws configurable from list of PEM-encoded resources. Removed key 
  defaults from main classpath, enforcing requirement for externally provided keys.
* Made the authentication display text configurable. 
  The `/auth/start` endpoint body has new parameter `language`, that determines what display text to show the user.
  The allowed values are `et`, `en` and `ru`.
  The default language is Estonian (`et`). The new configuration parameters are:
  * `app.auth.display-text.et`
  * `app.auth.display-text.en`
  * `app.auth.display-text.ru`
  * `app.auth.display-text.defaultLanguage`
* Added `/info` endpoint.
* Improved handling of client exceptions from MID/SID REST calls
* Made the issuer value for the session token configurable with the configuration parameter `app.session-token.issuer`.

## [0.6.0]

### Features:

* Mobile-ID support for session token creation
* Expired authorization processes are cleaned by a scheduled job. New configuration keys:
    * `app.cleanup.rate` - milliseconds between job executions, default '30000'
    * `app.cleanup.authProcessMaxAgeMinutes` - maximum allowable age for an auth process in
      minutes, default '5'
    * `app.cleanup.authProcessDeletionLimit` - limit to the number of records deleted by a
      single run of the cleanup job. default '1000'

### Improvements

* HTTP 404 Not Found returned by `/auth/status/{authProcessUuid}` when no auth process matching
  authProcessUuid found in database
* Switched to latest Spring Boot 3 from Spring Boot 4 to resolve constant Jackson version conflicts
  between Spring Boot and SK clients (smart-id-java-client, mid-rest-java-client)
* REST endpoint input validation errors are returned as HTTP 400 Bad Request with problem details.
* SID and MID processes can be configured to use different RP name and UUID values

## [0.5.0] First public release 