# Builds an asyncssh 2.8.1 server
FROM python:3.9-slim-bullseye

WORKDIR /usr/src/app

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY simple_server.py ./

ARG DEFAULT_USERNAME=sshattacker
ARG DEFAULT_PASSWORD=secret

ENV DEF_USER $DEFAULT_USERNAME
ENV DEF_PW $DEFAULT_PASSWORD

# Copy RSA server key
COPY /server_keys/ssh_host_key* ./server_keys/

# Copy authorized pubkeys
COPY authorized_keys/ ./authorized_keys/

# Switch back and run server
WORKDIR /usr/src/app
CMD [ "sh", "-c", "python ./simple_server.py $DEF_USER $DEF_PW" ]

EXPOSE 22
