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
RUN git clone --branch "v${VERSION}" --depth 1 https://github.com/jpillora/sshd-lite.git && cd sshd-lite && go build
LABEL ssh.implementation.name="sshd-lite" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT ["/go/sshd-lite/sshd-lite"]
CMD ["none"]
