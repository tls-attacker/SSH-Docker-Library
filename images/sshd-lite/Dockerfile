# Dockerfile for sshd-lite (based on Debian Bullseye)

# sshd-lite releases after 1.6 pull in x/crypto versions that need a newer
# Go than 1.19 (crypto/ecdh, slices). Go is backwards compatible, so one
# recent toolchain builds the whole version range.
ARG VERSION_GO=1.24
FROM golang:${VERSION_GO}-bookworm
ARG VERSION=1.3.1
# We want to use `cd` instead of `WORKDIR` to reduce the number of intermediate
# cache layers.
# hadolint ignore=DL3003
ENV GOTOOLCHAIN=auto
# Tags 1.23.1 and 1.36.0 ship a go.work whose `go` directive (1.25) is
# lower than what the module itself requires (>= 1.25.0), so the Go
# toolchain refuses the workspace. The workspace adds nothing for this
# single-module build, so ignore it; tags without go.work are unaffected.
ENV GOWORK=off
RUN git clone --branch "v${VERSION}" --depth 1 https://github.com/jpillora/sshd-lite.git && cd sshd-lite && go build
LABEL ssh.implementation.name="sshd-lite" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT ["/go/sshd-lite/sshd-lite"]
CMD ["none"]
