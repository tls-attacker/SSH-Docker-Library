#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh
#1.4.3-stable 1.4.2-stable 1.4.0-stable 1.3.0-stable 1.2.0-stable 1.1.0-stable 1.0.0-stable
versions=(${versions=1.4.11-stable 1.4.10-stable 1.4.8-stable 1.4.7-stable 1.4.6-stable 1.4.5-stable 1.4.4-stable 1.4.3-stable  })
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}wolfssh-client:${i} -f Dockerfile --target wolfssh-client .
done

exit "$EXITCODE"
