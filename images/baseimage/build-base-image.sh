#!/bin/bash
cd "$(dirname "$0")" || exit 1
source "../helper-functions.sh"

_docker build --build-arg VERSION=3.14 -t alpine-build:3.14 .

_docker build --build-arg VERSION=bullseye -f Dockerfile_debian -t debian-build:bullseye .
_docker build --build-arg VERSION=stretch --build-arg LIBSSL_VERSION=1.0 -f Dockerfile_debian -t debian-build:stretch-libssl1.0 .

exit "$EXITCODE"
