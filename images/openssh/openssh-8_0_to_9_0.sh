#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(${versions=8.0p1 8.1p1 8.2p1 8.3p1 8.4p1 8.5p1 8.6p1 8.7p1 8.8p1 9.0p1})
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}openssh-server:${i} -f Dockerfile-8_0_to_9_0 --target openssh-server .
done

exit "$EXITCODE"
