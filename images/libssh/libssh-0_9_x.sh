#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(0.9.0 0.9.1 0.9.2 0.9.3 0.9.4 0.9.5 0.9.6)
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}libssh-server:${i} -f Dockerfile-0_9_x --target libssh-server .
done

exit "$EXITCODE"
