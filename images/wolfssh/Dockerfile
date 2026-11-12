# Dockerfile for wolfssh 1.4.x (based on Debian Bullseye)

# `wolfssh-builder` is just an intermediate image used for building and does
# not require labels, so we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:bullseye AS wolfssh-builder
RUN sed -i -e "s|deb.debian.org/debian-security|archive.debian.org/debian-security|g" -e "s|security.debian.org|archive.debian.org|g" /etc/apt/sources.list && printf "Acquire::Check-Valid-Until \"false\";\n" > /etc/apt/apt.conf.d/99archive
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
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

# The two libraries must be matched: wolfSSH 1.4.15+ needs symbols
# (wc_ForceZero) that wolfSSL 5.6.3 does not export, while wolfSSH <= 1.4.14
# does not compile against wolfSSL 5.8.x at all ('heap' undeclared). Pick the
# wolfSSL release by the wolfSSH version unless VERSION_SSL is given.
# wolfSSH 1.6.0 is built against 5.9.1, the release its own CI tests against.
ARG VERSION
ARG VERSION_SSL=""
RUN if [ -z "${VERSION_SSL}" ]; then \
      if printf '%s\n%s\n' "${VERSION}" "1.4.14" | sort -V -C; then \
        VERSION_SSL="5.6.3"; \
      elif printf '%s\n%s\n' "1.6.0" "${VERSION}" | sort -V -C; then \
        VERSION_SSL="5.9.1"; \
      else \
        VERSION_SSL="5.8.4"; \
      fi; \
    fi && \
    mkdir /src && \
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

# Fast alle wolfSSH-Tags tragen das Suffix -stable; v1.4.16 nicht.
RUN if curl -fsSLI -o /dev/null "https://github.com/wolfSSL/wolfssh/archive/refs/tags/v${VERSION}-stable.tar.gz"; then \
      tag="v${VERSION}-stable"; dir="wolfssh-${VERSION}-stable"; \
    else \
      tag="v${VERSION}"; dir="wolfssh-${VERSION}"; \
    fi && \
    curl -LS "https://github.com/wolfSSL/wolfssh/archive/refs/tags/${tag}.tar.gz" | tar xzf - && \
    mv "${dir}" /src/wolfssh
WORKDIR /src/wolfssh
RUN ./autogen.sh && \
    ./configure --with-wolfssl=/install/wolfSSL --prefix=/install/wolfSSH --libdir=/install/wolfSSL/lib --includedir=/install/wolfSSL/include --enable-debug --enable-sshd && \
    LD_LIBRARY_PATH="/install/wolfSSL/lib" make && make install

# Server image
FROM debian:bullseye AS wolfssh-server
RUN sed -i -e "s|deb.debian.org/debian-security|archive.debian.org/debian-security|g" -e "s|security.debian.org|archive.debian.org|g" /etc/apt/sources.list && printf "Acquire::Check-Valid-Until \"false\";\n" > /etc/apt/apt.conf.d/99archive
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

COPY authorized_keys /install/authorized_keys
COPY sshd_config /install/sshd_config
COPY --from=wolfssh-builder /install/wolfSSL/lib* /lib/
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

# the example client accepts every host key; ACCEPT_HOSTKEY=no makes it reject them
RUN apt-get update && apt-get install -y --no-install-recommends patch && rm -rf /var/lib/apt/lists/*
COPY accept-hostkey.patch /src/
WORKDIR /src/wolfssh
RUN patch -p1 < /src/accept-hostkey.patch && LD_LIBRARY_PATH="/install/wolfSSL/lib" make
# answers the host key prompt as ACCEPT_HOSTKEY says (wolfSSH keeps no known hosts), see README.md
LABEL ssh.client.accept-hostkey="once,no"
ENTRYPOINT [ "/usr/local/bin/run-client.sh" ]
