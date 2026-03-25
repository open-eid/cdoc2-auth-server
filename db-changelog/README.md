# Liquibase changelog for CDOC2-auth-server

## Testing changes locally on a Postgres database

* `cd /db-changelog`
* `docker compose up`
* `mvn clean compile liquibase:update`
* `mvn liquibase:rollback -Dliquibase.rollbackCount={count_of_changesets_run_on_update}`