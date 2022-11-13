ARG VERSION_PYTHON=3.5

FROM python:${VERSION_PYTHON} AS paramiko-client
ARG VERSION
WORKDIR /usr/local/bin
RUN /usr/local/bin/python -m pip install --upgrade pip
#RUN pip install transport
RUN pip install six
RUN pip install paramiko==${VERSION}
RUN pip install click
COPY paramiko-client-script.py /usr/local/bin
COPY start.sh /usr/local/bin
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
LABEL "ssh_implementation"="paramiko"
LABEL "ssh_implementation_version"="${VERSION}"
LABEL "ssh_implementation_connectionRole"="client"
ENTRYPOINT ["client-entrypoint", "start.sh"]