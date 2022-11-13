ARG VERSION_PYTHON=3.9

FROM python:${VERSION_PYTHON} AS asyncssh-server

WORKDIR /app
COPY requirements.txt simple_server.py ./
ARG VERSION
RUN pip install --no-cache-dir -r requirements.txt asyncssh==$VERSION

ARG USERNAME=sshattacker
ARG PASSWORD=secret
ARG AUTHORIZED_KEYS_FILE=authorized_keys

# Copy RSA server key
COPY ssh_host_key* ./

# Copy authorized pubkeys
COPY "${AUTHORIZED_KEYS_FILE}" "authorized_keys"

LABEL ssh.implementation.name="asyncssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENV USERNAME="${USERNAME}" PASSWORD="${PASSWORD}"
# Although the JSON notation for CMD is preferable, there is no straightforward
# way to pass env vars, so we just disable the warning here.
# hadolint ignore=DL3025
CMD python simple_server.py "${USERNAME}" --password "${PASSWORD}" --authorized-keys "authorized_keys"
EXPOSE 22

FROM python:${VERSION_PYTHON} AS asyncssh-client
ARG VERSION
WORKDIR /usr/local/bin
COPY asyncssh-client-script.py /usr/local/bin
COPY start.sh /usr/local/bin
RUN /usr/local/bin/python -m pip install --upgrade pip
RUN pip install click
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
RUN pip install asyncssh==${VERSION}
LABEL "ssh_implementation"="asyncssh"
LABEL "ssh_implementation_version"="${VERSION}"
LABEL "ssh_implementation_connectionRole"="client"
ENTRYPOINT ["client-entrypoint", "start.sh"]
