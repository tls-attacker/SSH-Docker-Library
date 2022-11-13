ARG U_VERSION
FROM ubuntu:${U_VERSION} AS puttyLinux-client
ARG VERSION
WORKDIR /usr/local/bin
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
RUN apt-get update -y
RUN apt-cache madison putty-tools
RUN apt list -a putty-tools
RUN apt-get install -y putty-tools=${VERSION}
RUN apt-get install expect -y
LABEL "ssh_implementation"="putty"
LABEL "ssh_implementation_version"="${VERSION}"
LABEL "ssh_implementation_connectionRole"="client"
COPY login.exp /usr/local/bin/
COPY start.sh /usr/local/bin/
ENTRYPOINT ["client-entrypoint", "start.sh"]
