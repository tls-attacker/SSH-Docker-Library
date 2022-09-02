#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(0.6.5 0.7.0 0.7.1 0.7.2 0.7.3 0.7.4 0.7.5 0.7.6 0.7.7 0.8.0 0.8.1 0.8.2 0.8.3 0.8.4 0.8.5 0.8.6 0.8.7 0.8.8 0.8.9 0.9.0 0.9.1 0.9.2 0.9.3 0.9.4 0.9.5 0.9.6 0.10.0 0.10.2 0.10.3 0.10.4)
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}libssh-client:${i} -f Dockerfile --target libssh-client .
done

exit "$EXITCODE"

#0.6.5 0.7.0 0.7.1 0.7.2 0.7.3 0.7.4 0.7.5 0.7.6 0.7.7 0.8.0 0.8.1 0.8.2 0.8.3 0.8.4 0.8.5 0.8.6 0.8.7 0.8.8 0.8.9