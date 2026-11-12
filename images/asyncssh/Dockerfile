# hadolint ignore=DL3049
FROM python:3.11 AS asyncssh-base

WORKDIR /app
COPY files/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

ARG VERSION
RUN pip install --no-cache-dir asyncssh==$VERSION

# Server image
FROM asyncssh-base AS asyncssh-server

ARG USERNAME=sshattacker
ARG PASSWORD=secret
ARG AUTHORIZED_KEYS_FILE=authorized_keys

# Copy server script and RSA server key
COPY files/simple_server.py files/ssh_host_key* ./

# Copy authorized pubkeys
COPY "files/${AUTHORIZED_KEYS_FILE}" "authorized_keys"

LABEL ssh.implementation.name="asyncssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
# Environment variables are picked up by the simple_server.py script
ENV SSH_USERNAME="${USERNAME}" SSH_PASSWORD="${PASSWORD}"

ENTRYPOINT [ "python", "/app/simple_server.py", "-f", "authorized_keys" ]
EXPOSE 22

# Client image
FROM asyncssh-base AS asyncssh-client

# Copy client script
COPY files/simple_client.py ./

LABEL ssh.implementation.name="asyncssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"

# answers the host key prompt as ACCEPT_HOSTKEY says, see README.md
LABEL ssh.client.accept-hostkey="once,no"
ENTRYPOINT [ "python", "/app/simple_client.py" ]

# Automated client image: the client script is already non-interactive (it
# logs in with -u/-P and runs -c), so this is the client image under the
# client-automated name and type.
FROM asyncssh-client AS asyncssh-client-automated
LABEL ssh.implementation.type="client-automated"
