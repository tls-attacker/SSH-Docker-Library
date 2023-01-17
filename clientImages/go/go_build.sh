#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

versions=(${versions= v0.0.0-20220924013350-4ba4fb4dd9e7 v0.0.0-20220919173607-35f4265a4bc0 v0.0.0-20220829220503-c86fa9a7ed90 v0.0.0-20220722155217-630584e8d5aa v0.0.0-20220622213112-05595931fe9d v0.0.0-20220517005047-85d78b3ac167 v0.0.0-20220511200225-c6db032c6c88 v0.0.0-20220427172511-eb4f295cb31f v0.0.0-20220331220935-ae2d96664a29 v0.0.0-20220315160706-3147a52a75dd})
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}go-client:${i} -f Dockerfile --target go-client .
done

exit "$EXITCODE"
