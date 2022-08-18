FROM python:3

COPY requirements.txt ./
ARG ASYNCSSH_VERSION=2.12.0
RUN pip install --no-cache-dir -r requirements.txt asyncssh==$ASYNCSSH_VERSION
COPY simple_server.py ./

ARG ASYNCSSH_USERNAME=sshattacker
ARG ASYNCSSH_PASSWORD=bydahirsch

# Copy RSA server key
COPY ssh_host_key* ./

# Copy authorized pubkeys
COPY authorized_keys/ ./authorized_keys/

CMD ["python", "simple_server.py", "$ASYNCSSH_USER", "$ASYNCSSH_PASSWORD" ]
EXPOSE 22
