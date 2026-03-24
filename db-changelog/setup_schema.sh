#!/usr/bin/env bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER docker;
    CREATE DATABASE cdoc2auth;
    GRANT ALL PRIVILEGES ON DATABASE cdoc2auth TO docker;
    ALTER USER docker WITH PASSWORD 'docker';
EOSQL

psql -v ON_ERROR_STOP=1 --username docker --dbname cdoc2auth <<-EOSQL
    CREATE SCHEMA docker AUTHORIZATION docker;
EOSQL
