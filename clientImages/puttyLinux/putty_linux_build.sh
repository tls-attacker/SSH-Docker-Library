#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh


_docker build --build-arg VERSION=0.76-2 --build-arg U_VERSION=22.04 -t ${DOCKER_REPOSITORY}putty-client:0.76-2  -f Dockerfile --target putty-client .
_docker build --build-arg  VERSION=0.73-2 --build-arg U_VERSION=20.04 -t ${DOCKER_REPOSITORY}putty-client:0.73-2  -f Dockerfile --target putty-client .
_docker build --build-arg VERSION=0.70-4 --build-arg U_VERSION=18.04 -t ${DOCKER_REPOSITORY}putty-client:0.70-4  -f Dockerfile --target putty-client .

exit "$EXITCODE"
