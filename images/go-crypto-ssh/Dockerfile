# Dockerfile for the SSH implementation in golang.org/x/crypto/ssh.
#
# The Go module is the implementation under test; server.go and client.go are
# only the thin application around it, like simple_server.py in the asyncssh
# image. VERSION selects the x/crypto module version.

# hadolint ignore=DL3049
FROM golang:1.25-bookworm AS go-crypto-ssh-builder
WORKDIR /src
COPY files/server.go files/client.go ./
ARG VERSION
# Recent x/crypto releases raise their minimum Go version, so let the toolchain
# fetch whatever the selected module version asks for rather than pinning one.
ENV GOTOOLCHAIN=auto
RUN go mod init gocryptossh && \
    go get "golang.org/x/crypto@v${VERSION}" && \
    go mod tidy && \
    go build -o /out/ssh-server server.go && \
    go build -o /out/ssh-client client.go

# hadolint ignore=DL3049
FROM debian:bookworm-slim AS go-crypto-ssh-base
WORKDIR /app

# Server image
FROM go-crypto-ssh-base AS go-crypto-ssh-server
ARG USERNAME=sshattacker
ARG PASSWORD=secret
ARG AUTHORIZED_KEYS_FILE=authorized_keys
COPY files/ssh_host_key* ./
COPY "files/${AUTHORIZED_KEYS_FILE}" "authorized_keys"
COPY --from=go-crypto-ssh-builder /out/ssh-server /usr/local/bin/ssh-server
ARG VERSION
LABEL ssh.implementation.name="go-crypto-ssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENV SSH_USERNAME="${USERNAME}" SSH_PASSWORD="${PASSWORD}"
ENTRYPOINT [ "/usr/local/bin/ssh-server" ]
EXPOSE 22

# Client image
FROM go-crypto-ssh-base AS go-crypto-ssh-client
COPY --from=go-crypto-ssh-builder /out/ssh-client /usr/local/bin/ssh-client
ARG VERSION
LABEL ssh.implementation.name="go-crypto-ssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENTRYPOINT [ "/usr/local/bin/ssh-client" ]

# Automated client: files/automated_client.go logs in with a password, runs
# one command and answers the host key prompt as ACCEPT_HOSTKEY says.
# hadolint ignore=DL3049
FROM go-crypto-ssh-builder AS go-crypto-ssh-automated-builder
COPY files/automated_client.go ./
RUN go mod tidy && go build -o /out/ssh-client-automated automated_client.go

FROM go-crypto-ssh-base AS go-crypto-ssh-client-automated
COPY --from=go-crypto-ssh-automated-builder /out/ssh-client-automated /usr/local/bin/ssh-client-automated
ARG VERSION
LABEL ssh.implementation.name="go-crypto-ssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client-automated"
# answers the host key prompt as ACCEPT_HOSTKEY says, see README.md
LABEL ssh.client.accept-hostkey="yes,once,no"
ENTRYPOINT [ "/usr/local/bin/ssh-client-automated" ]
