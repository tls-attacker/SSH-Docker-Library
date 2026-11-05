# Dockerfile for the ssh2 npm module (pure-JavaScript SSH2 client and server).
#
# The npm package is the implementation under test; server.js and client.js are
# only the thin application around it.

# hadolint ignore=DL3049
FROM node:22-bookworm-slim AS node-ssh2-base
WORKDIR /app
ARG VERSION
RUN npm install --no-fund --no-audit "ssh2@${VERSION}"

# Server image
FROM node-ssh2-base AS node-ssh2-server
ARG USERNAME=sshattacker
ARG PASSWORD=secret
ARG AUTHORIZED_KEYS_FILE=authorized_keys
COPY files/server.js files/ssh_host_key ./
COPY "files/${AUTHORIZED_KEYS_FILE}" "authorized_keys"
ARG VERSION
LABEL ssh.implementation.name="node-ssh2" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENV SSH_USERNAME="${USERNAME}" SSH_PASSWORD="${PASSWORD}"
ENTRYPOINT [ "node", "/app/server.js" ]
EXPOSE 22

# Client image
FROM node-ssh2-base AS node-ssh2-client
COPY files/client.js ./
ARG VERSION
LABEL ssh.implementation.name="node-ssh2" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENTRYPOINT [ "node", "/app/client.js" ]
