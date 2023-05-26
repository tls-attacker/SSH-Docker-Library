ARG VERSION_PYTHON=3.5.10

FROM python:${VERSION_PYTHON} AS paramiko-client

SHELL ["/bin/bash", "-o", "pipefail", "-c"]
ARG VERSION
WORKDIR /usr/src
RUN /usr/local/bin/python -m pip install --upgrade pip
RUN pip install --no-cache-dir six==1.16.0 paramiko==${VERSION} click==8.1.3


COPY paramiko-client-script.py /usr/src
COPY paramiko-client-script-pubkey.py /usr/src/

#add pubke support to all versions after 2.8.0
RUN if printf '%s\n%s\n' "${VERSION}" "2.8.0" | sort -c -g; then \
        rm paramiko-client-script.py && mv paramiko-client-script-pubkey.py paramiko-client-script.py; \
    fi
ARG DOTSSH=dotssh-client
COPY /dotssh-client /usr/src/dotssh-client


LABEL ssh.implementation.name="paramiko" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"

ENTRYPOINT ["python", "paramiko-client-script.py"]
