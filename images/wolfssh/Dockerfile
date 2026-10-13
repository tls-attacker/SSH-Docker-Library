# Dockerfile for wolfssh 1.4.x (based on Debian Bullseye)

# `wolfssh-builder` is just an intermediate image used for building and does
# not require labels, so we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:bullseye AS wolfssh-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# bullseye is EOL; its security repo mirror serves a stale/expired Release
# file that apt refuses by default.
RUN sed -i -e 's|^# deb http://snapshot|deb http://snapshot|' -e 's|^deb http://deb.debian.org|# deb http://deb.debian.org|' /etc/apt/sources.list && \
    echo 'Acquire::Check-Valid-Until "false";' > /etc/apt/apt.conf.d/99no-check-valid-until
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        bsdextrautils \
        autoconf  \
        automake  \
        ca-certificates \
        curl \
        g++ \
        libtool  \
        make && \
    rm -rf /var/lib/apt/lists/*

ARG VERSION_SSL="5.6.3"
RUN mkdir  /src && \
    curl -LS "https://github.com/wolfSSL/wolfssl/archive/refs/tags/v${VERSION_SSL}-stable.tar.gz" | tar xzf - && \
    mv "wolfssl-${VERSION_SSL}-stable" /src/wolfssl
WORKDIR /src/wolfssl
# HAVE_ECC384/HAVE_ECC521 are not exposed as ./configure flags but gate
# wolfSSH's nistp384/nistp521 KEX and host-key algorithms (see
# wolfssh/internal.h); without them the client/server only ever offer
# nistp256, so they can't negotiate with a peer restricted to P-521 (e.g.
# SSH-State-Learner's default server config).
RUN ./autogen.sh && \
    CFLAGS="-DHAVE_ECC384 -DHAVE_ECC521" ./configure --enable-ssh --prefix=/install/wolfSSL --enable-cryptonly && \
    make && \
    make install

ARG VERSION
RUN curl -LS "https://github.com/wolfSSL/wolfssh/archive/refs/tags/v${VERSION}-stable.tar.gz" | tar xzf - &&\
    mv "wolfssh-${VERSION}-stable" /src/wolfssh
WORKDIR /src/wolfssh
RUN ./autogen.sh && \
    ./configure --with-wolfssl=/install/wolfSSL --prefix=/install/wolfSSH --libdir=/install/wolfSSL/lib --includedir=/install/wolfSSL/include --enable-debug --enable-sshd && \
    LD_LIBRARY_PATH="/install/wolfSSL/lib" make && make install

# Server image
FROM debian:bullseye AS wolfssh-server
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

COPY authorized_keys /install/authorized_keys
COPY sshd_config /install/sshd_config
COPY --from=wolfssh-builder /install/wolfSSL/lib* /lib/
COPY --from=wolfssh-builder /install/wolfSSH/lib* /lib/
COPY --from=wolfssh-builder /src/wolfssh/apps /install/apps/
COPY --from=wolfssh-builder /src/wolfssh/keys /keys

RUN useradd --system --no-create-home sshd && mkdir -p "/var/empty"

ARG USERNAME=sshattacker
ARG PASSWORD=secret
RUN useradd --create-home --groups users "${USERNAME}" && \
    echo "${USERNAME}:${PASSWORD}" | chpasswd
COPY --chown="${USERNAME}:${USERNAME}" "authorized_keys" "/home/${USERNAME}/.ssh/"
RUN chmod -R g=,o= "/home/${USERNAME}/.ssh"

ARG VERSION
LABEL "ssh.implementation.name"="wolfssh" \
    "ssh.implementation.version"="${VERSION}" \
    "ssh.implementation.type"="server"

ENTRYPOINT [ "/install/apps/wolfsshd/wolfsshd", \
    "-f", "/install/sshd_config",\
    "-d", "-D"]
EXPOSE 22

# Client image
# Currently based on wolfssh-builder as client will fail to start otherwise
FROM wolfssh-builder AS wolfssh-client
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

ARG VERSION
LABEL "ssh.implementation.name"="wolfssh" \
    "ssh.implementation.version"="${VERSION}" \
    "ssh.implementation.type"="client"

ENTRYPOINT [ "/src/wolfssh/examples/client/client" ]

# Automated client image
# The example client accepts host keys automatically and takes -u/-P/-c
# flags for non-interactive login and command execution, so no
# expect-driven wrapper is needed here, just argument translation.
FROM wolfssh-client AS wolfssh-client-automated
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

COPY run-client.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/run-client.sh

ARG VERSION
LABEL "ssh.implementation.name"="wolfssh" \
    "ssh.implementation.version"="${VERSION}" \
    "ssh.implementation.type"="client-automated"

# the example client accepts every host key; SSLC_HOSTKEY_PROMPT=reject makes it reject them
RUN apt-get update && apt-get install -y --no-install-recommends patch && rm -rf /var/lib/apt/lists/*
COPY hostkey-policy.patch /src/
WORKDIR /src/wolfssh
RUN patch -p1 < /src/hostkey-policy.patch && LD_LIBRARY_PATH="/install/wolfSSL/lib" make
# reads SSLC_HOSTKEY_PROMPT (wolfSSH keeps no known hosts), see SSH-State-Learner's host key handling
LABEL de.rub.nds.sslc.hostkey-policy="1"
ENTRYPOINT [ "/usr/local/bin/run-client.sh" ]
