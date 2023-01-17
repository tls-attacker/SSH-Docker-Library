FROM bberastegui/zgrab2:latest AS zgrab2-client
RUN apk update
RUN apk upgrade
RUN apk add bash
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
COPY input.txt /usr/local/bin/
COPY multiple.ini /usr/local/bin/
COPY start.sh /usr/local/bin/
LABEL "ssh_implementation"="zgrab2"
LABEL "ssh_implementation_version"="latest"
LABEL "ssh_implementation_connectionRole"="client"
WORKDIR /usr/local/bin/
ENTRYPOINT ["client-entrypoint", "start.sh"]

