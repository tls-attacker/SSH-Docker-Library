#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(${versions=8.8p1 9.0p1})
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}openssh-server:${i} -f Dockerfile --target openssh-server .
done

exit "$EXITCODE"
