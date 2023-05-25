ARG VERSION_PYTHON=3.5.10

FROM python:${VERSION_PYTHON} AS paramiko-client
ARG VERSION
WORKDIR /usr/src
RUN /usr/local/bin/python -m pip install --upgrade pip
RUN pip install six
RUN pip install paramiko==${VERSION}
RUN pip install click


COPY paramiko-client-script.py /usr/src
COPY paramiko-client-script-pubkey.py /usr/src/

#add pubke support to all versions after 2.8.0
RUN if printf '%s\n%s\n%s\n' "${VERSION}" "2.8.0" | sort -c -g; then \
        rm paramiko-client-script.py && mv paramiko-client-script-pubkey.py paramiko-client-script.py; \
    fi
ARG DOTSSH=dotssh-client
COPY /dotssh-client /usr/src/dotssh-client

ARG USERNAME
LABEL "ssh_implementation"="paramiko"
LABEL "ssh_implementation_version"="${VERSION}"
LABEL "ssh_implementation_connectionRole"="client"
ENTRYPOINT ["python", "paramiko-client-script.py"]
