#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh
exit_on_error

track_error ./libssh-0_7_0_to_0_8_9.sh
track_error ./libssh-0_9_x.sh

exit "$EXITCODE"
