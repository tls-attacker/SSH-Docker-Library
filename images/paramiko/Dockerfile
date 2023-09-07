ARG VERSION_PYTHON=3.11.5

FROM python:${VERSION_PYTHON} AS paramiko-client
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

WORKDIR /usr/src
RUN /usr/local/bin/python -m pip install --upgrade pip &&  \
    pip install --no-cache-dir six==1.16.0 click==8.1.3

ARG VERSION
RUN pip install --no-cache-dir paramiko==${VERSION}
COPY paramiko-client-script.py paramiko-client-script-pubkey.py /usr/src/

# Add publickey support to all versions after 2.8.0
RUN if printf '%s\n%s\n' "${VERSION}" "2.8.0" | sort -c -g; then \
        rm paramiko-client-script.py && mv paramiko-client-script-pubkey.py paramiko-client-script.py; \
    fi
ARG DOTSSH=dotssh-client
COPY "${DOTSSH}" /usr/src/.ssh

LABEL ssh.implementation.name="paramiko" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENTRYPOINT ["python", "paramiko-client-script.py"]
