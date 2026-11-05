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
ARG VERSION
ARG VERSION_SSL=""
RUN if [ -z "${VERSION_SSL}" ]; then \
      if printf '%s\n%s\n' "${VERSION}" "1.4.14" | sort -V -C; then \
        VERSION_SSL="5.6.3"; \
      else \
        VERSION_SSL="5.8.4"; \
      fi; \
    fi && \
    mkdir /src && \
    curl -LS "https://github.com/wolfSSL/wolfssl/archive/refs/tags/v${VERSION_SSL}-stable.tar.gz" | tar xzf - && \
    mv "wolfssl-${VERSION_SSL}-stable" /src/wolfssl
WORKDIR /src/wolfssl
RUN ./autogen.sh && \
    ./configure --enable-ssh --prefix=/install/wolfSSL --enable-cryptonly && \
    make && \
    make install

RUN curl -LS "https://github.com/wolfSSL/wolfssh/archive/refs/tags/v${VERSION}-stable.tar.gz" | tar xzf - &&\
    mv "wolfssh-${VERSION}-stable" /src/wolfssh
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
