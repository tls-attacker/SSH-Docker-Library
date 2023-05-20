# Dockerfile for libssh with mbedTLS (based on Debian Bullseye)

# `libssh-builder` is just an intermediate image used for building and does
# not require labels, so we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:bullseye AS libssh-mbedTLS-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    ca-certificates \
    curl \
    python3 \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*

ARG VERSION_MBEDTLS=2.28.3
RUN mkdir /src && \
    curl -LS "https://github.com/Mbed-TLS/mbedtls/archive/refs/tags/v${VERSION_MBEDTLS}.tar.gz" | tar xzf - && \
    mv "mbedtls-${VERSION_MBEDTLS}" /src/mbedtls && mkdir -p /install/mbedtls

WORKDIR /install/mbedtls
RUN /src/mbedtls/scripts/config.py set MBEDTLS_THREADING_C && /src/mbedtls/scripts/config.py set MBEDTLS_THREADING_PTHREAD &&\
    cmake -DUSE_SHARED_MBEDTLS_LIBRARY=On -DCMAKE_BUILD_TYPE=Release /src/mbedtls && \
    cmake --build . && make -j "$(nproc)" && make install

ARG VERSION=0.10.4
RUN curl -L "https://gitlab.com/libssh/libssh-mirror/-/archive/libssh-${VERSION}/libssh-mirror-libssh-${VERSION}.tar.gz" | tar xzf - && \
    mv "libssh-mirror-libssh-${VERSION}" /src/libssh
WORKDIR /src/libssh

ARG USERNAME=sshattacker
ARG PASSWORD=secret
# Make the old filename "ssh_server_fork.c" a hardlink to the new one in libssh
# >= 0.10.0 to make it compatible with this Dockerfile.
RUN if [ ! -e  examples/ssh_server_fork.c ]; then ln examples/ssh_server.c examples/ssh_server_fork.c ; fi
RUN sed -i "s/myuser/${USERNAME}/g" examples/ssh_server_fork.c && \
    sed -i "s/mypassword/${PASSWORD}/g" examples/ssh_server_fork.c


WORKDIR /src/libssh/build
RUN cmake -DWITH_MBEDTLS=ON -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=/install /src/libssh && \
    make -j "$(nproc)" && make install

FROM debian:bullseye AS libssh-mbedTLS-server
ARG VERSION
RUN apt-get update && apt-get install -y --no-install-recommends \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*


ARG AUTHORIZED_KEYS_FILE=authorized_keys
COPY "${AUTHORIZED_KEYS_FILE}" /install/authorized_keys
COPY host_keys /install/host_keys/
COPY --from=libssh-mbedTLS-builder /install /usr/

COPY --from=libssh-mbedTLS-builder /install/mbedtls/include/mbedtls /usr/local/include/mbedtls/
COPY --from=libssh-mbedTLS-builder /install/mbedtls/include/psa /usr/local/psa/

COPY --from=libssh-mbedTLS-builder /install/mbedtls/library/libmbed* /usr/lib/

COPY --from=libssh-mbedTLS-builder /src/libssh/build/examples /install/examples/

LABEL "ssh.implementation.name"="libssh_mbedTLS" \
    "ssh.implementation.version"="${VERSION}" \
    "ssh.implementation.type"="server"
CMD [ "/install/examples/ssh_server_fork", \
    "-p", "22", \
    "-k", "/install/host_keys/ssh_host_dsa_key", \
    "-k", "/install/host_keys/ssh_host_ecdsa_key", \
    "-k", "/install/host_keys/ssh_host_rsa_key", \
    "-k", "/install/host_keys/ssh_host_ed25519_key", \
    "-a", "/install/authorized_keys", \
    "-u", "${USERNAME}",\
    "-p", "${PASSWORD}",\
    "-v",\
    "0.0.0.0" ]
EXPOSE 22
