#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh

#versions=(${versions=0.23 0.255 0.28 0.29 0.30 0.31 0.32 0.33 0.34 0.35 0.36 0.37 0.38 0.39 0.40 0.41 0.42 0.43 0.44 0.45 0.46 0.47 0.49 0.50 0.51 0.52 0.53.1 0.53 2011.54 2012.55 2013.56 2013.57 2013.58 2013.59 2013.60 2013.62 2014.63 2014.64 2014.65 2014.66 2015.67 2015.68 2015.69 2015.70 2015.71 2016.72 2016.73 2016.74 2017.75 2018.76 2019.77 2019.78 2020.79 2020.80 2020.81 2022.82})
versions=(${versions=0.43 0.44 0.45 0.46 0.47 0.49 0.50})
for i in "${versions[@]}"; do
  _docker build --build-arg VERSION=${i} -t ${DOCKER_REPOSITORY}dropbear-client:${i} -f Dockerfile --target dropbear-client .
done

exit "$EXITCODE"
