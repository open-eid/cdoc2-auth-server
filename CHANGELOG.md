# Changelog

## [unreleased]

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
* Upgraded to Spring Boot 4.0.6
* SID and MID processes can be configured to use different RP name and UUID values

## [0.5.0] First public release 