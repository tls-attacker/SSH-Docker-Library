FROM python:3

COPY requirements.txt ./
ARG VERSION
RUN pip install --no-cache-dir -r requirements.txt asyncssh==$VERSION
COPY simple_server.py ./

ARG USERNAME=sshattacker
ARG PASSWORD=bydahirsch
ARG AUTHORIZED_KEYS_FILE=authorized_keys/sshattacker

# Copy RSA server key
COPY ssh_host_key* ./

# Copy authorized pubkeys
COPY "${AUTHORIZED_KEYS_FILE}" "authorized_keys"

LABEL ssh.implementation.name="asyncssh" \
      ssh.implementation.version="${VERSION}"
ENV USERNAME="${USERNAME}" PASSWORD="${PASSWORD}"
CMD python simple_server.py "${USERNAME}" --password "${PASSWORD}" --authorized-keys "authorized_keys"
EXPOSE 22
