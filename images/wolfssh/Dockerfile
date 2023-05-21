# Dockerfile for wolfssh 0.9.x (based on Debian Bullseye)

# `wolfssh-builder` is just an intermediate image used for building and does
# not require labels, so we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:bullseye AS wolfssh-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    autoconf  \
    automake  \
    ca-certificates \
    curl \
    g++ \
    libtool  \
    make \
    && rm -rf /var/lib/apt/lists/*

ARG VERSION_SSL

RUN mkdir  /src && \
    curl -LS "https://github.com/wolfSSL/wolfssl/archive/refs/tags/v${VERSION_SSL}-stable.tar.gz" | tar xzf - && \
    mv "wolfssl-${VERSION_SSL}-stable" /src/wolfssl

ARG VERSION
RUN curl -LS "https://github.com/wolfSSL/wolfssh/archive/refs/tags/v${VERSION}-stable.tar.gz" | tar xzf - &&\
    mv "wolfssh-${VERSION}-stable" /src/wolfssh

WORKDIR /src/wolfssl

RUN ./autogen.sh && \
    ./configure --enable-ssh --prefix=/install/wolfSSL --enable-cryptonly && \
    make && \
    make install

WORKDIR /src/wolfssh/
# Change username and password in demo application as older versions do not support cmd option
ARG USERNAME=sshattacker
ARG PASSWORD=secret
RUN sed -i "s/jill/${USERNAME}/g" examples/echoserver/echoserver.c && \
    sed -i "s/upthehill/${PASSWORD}/g" examples/echoserver/echoserver.c

RUN ./autogen.sh && \
    ./configure --with-wolfssl=/install/wolfSSL --prefix=/install/wolfSSH --libdir=/install/wolfSSL/lib --includedir=/install/wolfSSL/include --enable-debug && \
    LD_LIBRARY_PATH="/install/wolfSSL/lib" make && make install

FROM debian:bullseye AS wolfssh-server
ARG VERSION

COPY authorized_keys /install/authorized_keys/
COPY --from=wolfssh-builder /install/wolfSSL/lib* /lib/
COPY --from=wolfssh-builder /install/wolfSSH/lib* /lib/
COPY --from=wolfssh-builder /src/wolfssh/examples /install/examples/
COPY --from=wolfssh-builder /src/wolfssh/keys /keys
LABEL "ssh.implementation.name"="wolfssh" \
    "ssh.implementation.version"="${VERSION}" \
    "ssh.implementation.type"="server"
CMD [ "/install/examples/echoserver/echoserver", \
    "-p", "22",\
    "-j", "/install/authorized_keys", \
    "-f", "-E"]

EXPOSE 22
