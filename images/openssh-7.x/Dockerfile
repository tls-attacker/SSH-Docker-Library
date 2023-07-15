# Dockerfile for OpenSSH 7 (based on Debian Stretch)

ARG USERNAME=sshattacker
ARG PASSWORD=secret

# `openssh-builder` is just an intermediate image and does not require labels,
# so we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:bullseye AS openssh-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    ca-certificates \
    curl \
    libssl-dev \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*
ARG VERSION
RUN mkdir /src && curl -s https://cloudflare.cdn.openbsd.org/pub/OpenBSD/OpenSSH/portable/openssh-${VERSION}.tar.gz | tar xzf - -C /src
COPY patches /src
WORKDIR "/src/openssh-${VERSION}"

# For OpenSSH 7.8+, the variable names inside a `DEBUG_KEXDH` ifdef block went
# out of sync. We need to apply these patches to be able to build with
# `DEBUG_KEXDH` enabled.
RUN if printf "%s\n%s\n" "7.8" "${VERSION}" | sort -c -g; then \
        patch -p1 < /src/0001-Fix-build-failure-caused-by-wrong-variable-name-in-D-v7.8-7.9.patch; \
    fi

# If `WITH_NONE_CIPHER` is not zero, patch the OpenSSL version to allow using
# the `none` cipher.
ARG WITH_NONE_CIPHER=1
RUN if [ "${WITH_NONE_CIPHER}" -ne 0 ]; then \
        if printf "%s\n%s\n" "7.6" "${VERSION}" | sort -c -g; then \
            patch -p1 < /src/0001-Enable-support-for-none-cipher-v7.6-v9.0.patch; \
        else \
            patch -p1 < /src/0001-Enable-support-for-none-cipher-v7.0-v7.5.patch; \
        fi; \
    fi

# Enable SSHv1 while compliation if needed
ARG WITH_SSHV1=0
RUN if [ "${WITH_SSHV1}" -ne 0 ]; then \
        ./configure CFLAGS="-DDEBUG_KEX=1 -DDEBUG_KEXDH=1 -DDEBUG_KEXECDH=1" --with-ssh1 --prefix /install && \
            make -j "$(nproc)" && \
            make install-files install-sysconf host-key; \
    else \
        ./configure CFLAGS="-DDEBUG_KEX=1 -DDEBUG_KEXDH=1 -DDEBUG_KEXECDH=1" --prefix /install && \
            make -j "$(nproc)" && \
            make install-files install-sysconf host-key; \
    fi


# Common for client and server image.
#
# `openssh-base` is just an intermediate image and does not require labels, so
# we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:bullseye AS openssh-base
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl-dev \
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

# Rate Limit: Increase `MaxStartups` to make it unlikely to trigger the
# `Exceeded MaxStartups\r\n` issue, even when opening a lot of connections.
#
# FIXME: Although the server starts fine, login seems to be broken for 8.0p1
# and 8.1p1. The server simply closes the connection.
ENTRYPOINT ["/install/sbin/sshd", "-D", "-e", "-o", "MaxStartups=65536"]
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
