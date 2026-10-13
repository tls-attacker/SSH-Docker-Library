#!/usr/bin/env bash
# Builds exactly the Docker images that SSH-State-Learner's client controllers
# (SSH-State-Learner/client-worker/.../desktopclients/controller/Docker*ClientController.kt)
# request, at the versions currently pinned via --client-sul-docker-tag in
# ../experiments/learnerconfiguration/client/*.args.
#
# Unlike build-clients.sh (which builds every recent version of every
# client-automated image in the library), this only builds the ~8 client SUL
# images SSH-State-Learner's DockerControllerManager actually knows how to
# drive, at one version each - the version an experiment run will actually
# request. Re-run this after bumping a version in experiments/learnerconfiguration/client/*.args
# or after changing a controller's dockerImageTag.
set -euo pipefail
cd "$(dirname "$0")"

# --client-sul value (from the *.args files) -> compose service name prefix
# (as defined in images/<tool>/compose.yml). Must match the dockerImageTag
# each Docker*ClientController.kt uses, minus the ":<version>" suffix.
declare -A SERVICE_PREFIX=(
  [ASYNCSSH]=asyncssh-client-automated
  [OPENSSH]=openssh-client-automated
  [DROPBEAR_DOCKER]=dropbear-client-automated
  [GOSSH_DOCKER]=golang-ssh-client-automated
  [PARAMIKO_DOCKER]=paramiko-client
  [RUSSH_DOCKER]=russh-client
  [ERLANGSSH]=erlang-ssh-client-automated
  [LIBSSH]=libssh-client-automated
  [WOLFSSH]=wolfssh-client-automated
)

# Fallback versions for client SULs not (yet) pinned in an experiments/*.args
# file. Update these if you start experimenting with these clients, or add a
# client/*.args file with --client-sul-docker-tag set instead.
declare -A DEFAULT_VERSIONS=(
  [ERLANGSSH]=27.3.0.0
  [LIBSSH]=0.11.2
  [WOLFSSH]=1.4.14
)

ARGS_DIR="${ARGS_DIR:-../experiments/learnerconfiguration/client}"

declare -A ARGS_VERSION
if [ -d "$ARGS_DIR" ]; then
  for f in "$ARGS_DIR"/*.args; do
    [ -e "$f" ] || continue
    mapfile -t lines <"$f"
    sul=""
    tag=""
    for i in "${!lines[@]}"; do
      case "${lines[$i]}" in
      --client-sul) sul="${lines[$((i + 1))]}" ;;
      --client-sul-docker-tag) tag="${lines[$((i + 1))]}" ;;
      esac
    done
    if [ -n "$sul" ] && [ -n "$tag" ]; then
      ARGS_VERSION[$sul]="$tag"
    fi
  done
else
  echo "Warning: $ARGS_DIR not found, using only DEFAULT_VERSIONS" >&2
fi

failed=()
built=()
skipped=()

for sul in "${!SERVICE_PREFIX[@]}"; do
  version="${ARGS_VERSION[$sul]:-${DEFAULT_VERSIONS[$sul]:-}}"
  if [ -z "$version" ]; then
    echo "Skipping $sul: no version pinned in $ARGS_DIR/*.args and no DEFAULT_VERSIONS entry" >&2
    skipped+=("$sul")
    continue
  fi
  service="${SERVICE_PREFIX[$sul]}-${version}"
  echo "=== Building $sul -> $service ==="
  if docker compose build "$service"; then
    built+=("$service")
  else
    failed+=("$service")
  fi
done

echo ""
echo "=== Summary ==="
echo "Built (${#built[@]}): ${built[*]:-none}"
echo "Skipped (${#skipped[@]}): ${skipped[*]:-none}"
echo "Failed (${#failed[@]}): ${failed[*]:-none}"

if [ "${#failed[@]}" -gt 0 ]; then
  exit 1
fi
