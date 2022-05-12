#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(${versions=7.0p1 7.1p1 7.2p1 7.2p2 7.3p1 7.4p1 7.5p1 7.6p1 7.7p1 7.8p1 7.9p1})
with_none=(${with_none=true})

for i in "${versions[@]}"; do
#transfers the needed patch, for the use of none cipher, if cipher none should be included
  if [ "$with_none" = true ] ; then
    if [ "${i}" == "7.0p1" ] || [ "${i}" == "7.1p1" ] || [ "${i}" == "7.2p1" ] || [ "${i}" == "7.3p1" ] || [ "${i}" == "7.4p1" ] || [ "${i}" == "7.5p1" ] ; then
      _docker build --build-arg VERSION=${i} --build-arg NONECIPHERPATCH="v7.0-5none-cipher.patch" -t ${DOCKER_REPOSITORY}openssh-server:${i} -f Dockerfile-7_x --target openssh-server .
    else
      _docker build --build-arg VERSION=${i} --build-arg NONECIPHERPATCH="v7.6-9.0none-cipher.patch" -t ${DOCKER_REPOSITORY}openssh-server:${i} -f Dockerfile-7_x --target openssh-server .
    fi
  else
    _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}openssh-server:${i} -f Dockerfile-7_x --target openssh-server .
  fi
done

exit "$EXITCODE"
