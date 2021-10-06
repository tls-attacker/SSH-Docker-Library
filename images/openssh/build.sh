#!/bin/bash
cd "$(dirname "$0")" || exit 1
source ../helper-functions.sh
exit_on_error

track_error ./openssh-7_x.sh
track_error ./openssh-8_x.sh

exit "$EXITCODE"