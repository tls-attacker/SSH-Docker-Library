# Dockerfile for libssh 0.7.x - 0.8.x (based on Debian Stretch)

# The TLS version from debian stretch image is too old to be able to download
# the libssh tarball. Hence, we use a recent Alpine linux image to fetch
# the source.
#
# `libssh-downloader` is just an intermediate image and does not require
# labels, so we can disable that linter rule.
# hadolint ignore=DL3049
FROM alpine:3.16 AS libssh-downloader
SHELL ["/bin/ash", "-eo", "pipefail", "-c"]
RUN apk add --no-cache curl tar
ARG VERSION
RUN curl -s "https://gitlab.com/libssh/libssh-mirror/-/archive/libssh-${VERSION}/libssh-mirror-libssh-${VERSION}.tar.gz" | tar xzf -

# After download in , we can continue on debian stretch and start the build.
#
# `libssh-builder` is just an intermediate image used for building and does
# not require labels, so we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:stretch AS libssh-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    libssl1.0-dev \
    zlib1g-dev \
    python3 \
    && rm -rf /var/lib/apt/lists/*
ARG VERSION
COPY --from=libssh-downloader /libssh-mirror-libssh-${VERSION} /src/libssh
WORKDIR /src/libssh

ARG USERNAME=sshattacker
ARG PASSWORD=secret
RUN sed -i "s/myuser/${USERNAME}/g" examples/ssh_server_fork.c && \
    sed -i "s/mypassword/${PASSWORD}/g" examples/ssh_server_fork.c

# Apply patches to fix the build for some very old versions.
COPY patches/ .
RUN if printf '%s\n%s\n%s\n' "0.8.2" "${VERSION}" "0.8.6" | sort -c -g; then \
        patch -p1 < 0001-0.8.2-0.8.6-Disable-Werror-strict-overflow-to-fix-ab.patch ; \
    fi

WORKDIR /src/libssh/build
RUN cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=/install .. && \
    make -j "$(nproc)" && \
    make install

FROM debian:stretch AS libssh-server
ARG VERSION
RUN apt-get update && apt-get install -y --no-install-recommends \
  libssl1.0-dev \
  zlib1g-dev \
  && rm -rf /var/lib/apt/lists/*
COPY host_keys /install/host_keys/
COPY --from=libssh-builder /install /usr/
COPY --from=libssh-builder /src/libssh/build/examples /install/examples/
LABEL "ssh.implementation.name"="libssh" \
    "ssh.implementation.version"="${VERSION}" \
    "ssh.implementation.type"="server"
CMD [ "/install/examples/ssh_server_fork", \
  "-p", "22", \
  "-k", "/install/host_keys/ssh_host_dsa_key", \
  "-k", "/install/host_keys/ssh_host_rsa_key", \
  "-k", "/install/host_keys/ssh_host_ecdsa_key", \
  "0.0.0.0" ]
EXPOSE 22
