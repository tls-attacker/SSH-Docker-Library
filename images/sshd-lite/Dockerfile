# Dockerfile for sshd-lite (based on Debian Bullseye)

FROM golang:1.19-bullseye
ARG VERSION=1.3.1
# We want to use `cd` instead of `WORKDIR` to reduce the number of intermediate
# cache layers.
# hadolint ignore=DL3003
RUN git clone --branch "v${VERSION}" --depth 1 https://github.com/jpillora/sshd-lite.git && cd sshd-lite && go build
LABEL ssh.implementation.name="sshd-lite" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT ["/go/sshd-lite/sshd-lite"]
CMD ["none"]
