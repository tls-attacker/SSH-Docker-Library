#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(${versions=8.0p1 8.1p1 8.2p1 8.3p1 8.4p1 8.5p1 8.6p1 8.7p1 8.8p1 9.0p1})
with_none=(${with_none=true})

for i in "${versions[@]}"; do
#transfers the needed patch, for the use of none cipher, if cipher none should be included
  if [ "$with_none" = true ] ; then
      _docker build --build-arg VERSION=${i} --build-arg NONECIPHERPATCH="v7.6-8.9none-cipher.patch" -t ${DOCKER_REPOSITORY}openssh-server:${i} -f  Dockerfile-8_0_to_9_0 --target openssh-server .
  else
     _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}openssh-server:${i} -f Dockerfile-8_0_to_9_0 --target openssh-server .
  fi 
done

exit "$EXITCODE"
