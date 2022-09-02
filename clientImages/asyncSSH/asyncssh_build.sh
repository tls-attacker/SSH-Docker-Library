#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(${versions="2.12.0" "2.11.0" "2.10.0" "2.9.0" "2.8.1" "2.8.0" "2.7.2" "2.7.1" "2.7.0" "2.6.0" "2.5.0" "2.4.2" "2.4.1" "2.4.0" "2.3.0"  "2.2.1"  "2.2.0" "2.1.0" "2.0.1" "2.0.0" "1.18.0" "1.17.1" "1.17.0" "1.16.1"})
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}asyncssh-client:${i} -f Dockerfile --target asyncssh-client .
done

exit "$EXITCODE"
