#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(${versions="2.11.0" "2.10.5" "2.10.4" "2.10.3" "2.10.2" "2.10.1" "2.10.0"})
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}paramiko-client:${i} -f Dockerfile --target paramiko-client .
done

exit "$EXITCODE"
