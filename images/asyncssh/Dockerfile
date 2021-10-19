FROM python:3

WORKDIR /usr/src/app

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY simple_server.py ./

ARG DEFAULT_USERNAME=sshattacker
ARG DEFAULT_PASSWORD=bydahirsch

ENV DEF_USER $DEFAULT_USERNAME
ENV DEF_PW $DEFAULT_PASSWORD

# Copy RSA server key
COPY ssh_host_key* ./

# Copy authorized pubkeys
COPY authorized_keys/ ./authorized_keys/

CMD [ "sh", "-c", "python ./simple_server.py $DEF_USER $DEF_PW" ]

EXPOSE 22