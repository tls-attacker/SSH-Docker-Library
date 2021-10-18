FROM python:3

WORKDIR /usr/src/app

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY simple_server.py ./

# Copy RSA server key
COPY ssh_host_key* ./

# Copy authorized pubkeys
COPY authorized_keys/ ./authorized_keys/

CMD [ "python", "./simple_server.py" ]

EXPOSE 8022