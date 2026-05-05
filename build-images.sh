#!/usr/bin/env bash
# build Docker image locally
#set -x

AUTH_SERVER_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
DOCKER_REGISTRY=ghcr.io
DOCKER_REPOSITORY=open-eid
IMAGE_NAME=cdoc2-auth-server

# install all modules to local repo so webapp can resolve local dependencies
mvn install -Dmaven.test.skip=true

mvn spring-boot:build-image \
  -pl webapp \
  -Dmaven.test.skip=true \
  -Dspring-boot.build-image.publish=false \
  -Dspring-boot.build-image.imageName=${DOCKER_REGISTRY}/${DOCKER_REPOSITORY}/${IMAGE_NAME}:${AUTH_SERVER_VERSION} \
  -Dspring-boot.build-image.tags=${DOCKER_REGISTRY}/${DOCKER_REPOSITORY}/${IMAGE_NAME}:latest \
  -Dspring-boot.build-image.createdDate=now
