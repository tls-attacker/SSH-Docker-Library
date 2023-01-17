#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(${versions=7.0p1 7.1p1 7.2p1 7.2p2 7.3p1 7.4p1 7.5p1 7.6p1 7.7p1 8.4p1 8.5p1 8.6p1 8.7p1 8.8p1 9.0p1})
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}openssh-client:${i} -f Dockerfile --target openssh-client .
done

versions2=(${versions2=8.0p1 8.1p1 8.2p1 8.3p1})
for j in "${versions2[@]}"; do
  _docker build --build-arg VERSION=${j} -t ${DOCKER_REPOSITORY}openssh-client:${j} -f Dockerfile2 --target openssh-client .
done

versions3=(${versions3=7.8p1 7.9p1})
for k in "${versions3[@]}"; do
  _docker build --build-arg VERSION=${k} -t ${DOCKER_REPOSITORY}openssh-client:${k} -f Dockerfile3 --target openssh-client .
done

exit "$EXITCODE"
