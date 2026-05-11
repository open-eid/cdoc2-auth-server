# Changelog

## [unreleased]

### Features:

* Expired authorization processes are cleaned by a scheduled job. New configuration keys:
    * `app.cleanup.rate` - milliseconds between job executions
    * `app.cleanup.authProcessMaxAgeMinutes` - maximum allowable age for an auth process in minutes

### Improvements

* HTTP 404 Not Found returned by `/auth/status/{authProcessUuid}` when no auth process matching
  authProcessUuid found in database

## [0.5.0] First public release 