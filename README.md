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
