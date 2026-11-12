#!/bin/bash
set -euo pipefail

ip="$1"
port="$2"
user="$3"
password="$4"
command="$5"

exec /src/wolfssh/examples/client/client -h "$ip" -p "$port" -u "$user" -P "$password" -c "$command"
