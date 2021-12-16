#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(${versions=7.0p1 7.1p1 7.2p1 7.2p2 7.3p1 7.4p1 7.5p1 7.6p1 7.7p1 7.8p1 7.9p1})
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}openssh-server:${i} -f Dockerfile-7_x --target openssh-server .
done

exit "$EXITCODE"
