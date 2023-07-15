# Dockerfile for OpenSSH 7 (based on Debian Stretch)

ARG USERNAME=sshattacker
ARG PASSWORD=secret

# The TLS version from debian stretch image is too old to be able to download
# the OpenSSH tarball. Hence, we use a recent Alpine linux image to fetch
# the source.
#
# `openssh-downloader` is just an intermediate image and does not require
# labels, so we can disable that linter rule.
# hadolint ignore=DL3049
FROM alpine:3.16 AS openssh-downloader
SHELL ["/bin/ash", "-eo", "pipefail", "-c"]
RUN apk add --no-cache curl tar
ARG VERSION
RUN curl -s https://cloudflare.cdn.openbsd.org/pub/OpenBSD/OpenSSH/portable/openssh-${VERSION}.tar.gz | tar xzf -

# After download, we can continue on debian stretch and start the build.
#
# `openssh-builder` is just an intermediate image used for building and does
# not require labels, so we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:stretch AS openssh-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libssl1.0-dev \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*
ARG VERSION
COPY --from=openssh-downloader /openssh-${VERSION} /src/openssh-${VERSION}
COPY patches /src/
WORKDIR /src/openssh-${VERSION}

RUN ./configure CFLAGS="-DDEBUG_KEX=1 -DDEBUG_KEXDH=1 -DDEBUG_KEXECDH=1" --prefix /install && \
            make -j "$(nproc)" && \
            make install-files install-sysconf host-key
# Common for client and server image.
#
# `openssh-base` is just an intermediate image and does not require labels, so
# we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:stretch AS openssh-base
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl1.0-dev \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*

ARG USERNAME
ARG PASSWORD
RUN useradd --create-home --groups users "${USERNAME}" && \
    echo "${USERNAME}:${PASSWORD}" | chpasswd

# Create a minimal server image without all the compile-time dependencies from
# the previous step.
FROM openssh-base AS openssh-server

RUN useradd --system --no-create-home sshd && mkdir -p "/var/empty"

# Set up SSH user, add authorized keys and fix `.ssh` dir permissions
ARG USERNAME
ARG DOTSSH=dotssh-server
COPY --chown="${USERNAME}:${USERNAME}" "${DOTSSH}" "/home/${USERNAME}/.ssh/"
RUN chmod -R g=,o= "/home/${USERNAME}/.ssh"

ARG VERSION
COPY --from=openssh-builder /install /install
LABEL ssh.implementation.name="openssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"

# Add SSHv1 Support in sshd_config
ARG WITH_SSHV1=0
RUN if [ "${WITH_SSHV1}" -ne 0 ]; then \
        sed -i  's/#Protocol 2/Protocol 1,2/g' /install/etc/sshd_config; \
    fi

ENTRYPOINT ["/install/sbin/sshd", "-D", "-e"]
EXPOSE 22

# Client image
FROM openssh-base AS openssh-client

ARG USERNAME
ARG DOTSSH=dotssh-client

COPY --chown="${USERNAME}:${USERNAME}" "${DOTSSH}" "/home/${USERNAME}/.ssh/"
RUN chmod -R g=,o= "/home/${USERNAME}/.ssh"

ARG VERSION
COPY --from=openssh-builder /install /install
LABEL ssh.implementation.name="openssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
      
ENTRYPOINT ["/install/bin/ssh"]
