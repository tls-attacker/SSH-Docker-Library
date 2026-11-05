# Dockerfile for Twisted Conch (the SSH implementation inside Twisted).
#
# Twisted is the implementation under test; server.py is only the thin
# application around it, and `conch` is Twisted's own client program.

# hadolint ignore=DL3049
FROM python:3.12-slim AS twisted-conch-base
WORKDIR /app
ARG VERSION
RUN pip install --no-cache-dir "twisted[conch]==${VERSION}"

# Server image
FROM twisted-conch-base AS twisted-conch-server
ARG USERNAME=sshattacker
ARG PASSWORD=secret
ARG AUTHORIZED_KEYS_FILE=authorized_keys
COPY files/server.py files/ssh_host_key* ./
COPY "files/${AUTHORIZED_KEYS_FILE}" "authorized_keys"
ARG VERSION
LABEL ssh.implementation.name="twisted-conch" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENV SSH_USERNAME="${USERNAME}" SSH_PASSWORD="${PASSWORD}"
ENTRYPOINT [ "python", "/app/server.py" ]
EXPOSE 22

# Client image
FROM twisted-conch-base AS twisted-conch-client
COPY files/id_rsa /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa
ARG VERSION
LABEL ssh.implementation.name="twisted-conch" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENTRYPOINT [ "conch" ]
