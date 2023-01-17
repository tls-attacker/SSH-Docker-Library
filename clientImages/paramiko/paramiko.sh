#!/bin/bash
cd "$(dirname "$0")" || exit 1
source "../../helper-functions.sh"

#_docker build --build-arg VERSION=3.14 -t alpine-build:3.14 .
#
_docker build --build-arg VERSION=latest -f Dockerfile -t parmiko-client:1.0 .

exit "$EXITCODE"