# Changelog

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