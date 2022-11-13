FROM debian-build:bullseye AS wolfssh-downloader
ARG VERSION
WORKDIR /src
RUN wget -q https://github.com/wolfSSL/wolfssh/archive/refs/tags/v${VERSION}.tar.gz
RUN tar -xf v${VERSION}.tar.gz

FROM debian-build:bullseye AS wolfssh-client
ARG VERSION
COPY --from=wolfssh-downloader /src/wolfssh-${VERSION} /src/wolfssh-${VERSION}
WORKDIR /src/wolfssh-${VERSION}
RUN apt-get update
RUN apt-get install -y libtool
RUN apt-get install -y libwolfssl-dev
RUN apt-get install -y autoconf
RUN ./autogen.sh
RUN ./configure --enable-ssh
RUN make
RUN make check
RUN make install
LABEL "ssh_implementation"="wolfssh"
LABEL "ssh_implementation_version"="${VERSION}"
LABEL "ssh_implementation_connectionRole"="client"
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
WORKDIR /src/wolfssh-${VERSION}/examples/client
RUN ./client -h
ENTRYPOINT ["client-entrypoint", "./client"]