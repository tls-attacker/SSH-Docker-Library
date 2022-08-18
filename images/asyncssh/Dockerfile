FROM python:3

COPY requirements.txt ./
ARG VERSION
RUN pip install --no-cache-dir -r requirements.txt asyncssh==$VERSION
COPY simple_server.py ./

ARG USERNAME=sshattacker
ARG PASSWORD=bydahirsch

# Copy RSA server key
COPY ssh_host_key* ./

# Copy authorized pubkeys
COPY authorized_keys/ ./authorized_keys/

CMD ["python", "simple_server.py", "$USERNAME", "$PASSWORD" ]
EXPOSE 22
