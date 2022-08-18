# Dockerfile for OpenSSH 7 (based on Debian Stretch)
#
# The TLS version from debian strecth image is too old to be able to download
# the OpenSSH tarball. Hence, we use a recent Alpine linux image to fetch
# the source.
FROM alpine:latest AS openssh-downloader
RUN apk add --update curl tar
ARG VERSION
RUN curl -s https://cloudflare.cdn.openbsd.org/pub/OpenBSD/OpenSSH/portable/openssh-${VERSION}.tar.gz | tar xzf -

# After download in , we can continue on debian stretch and start the build.
FROM debian:stretch AS openssh-builder
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libssl1.0-dev \
    zlib1g-dev
ARG VERSION
COPY --from=openssh-downloader /openssh-${VERSION} /src/openssh-${VERSION}
ADD patches /src
WORKDIR /src/openssh-${VERSION}

# For OpenSSH 7.8+, the variable names inside a `DEBUG_KEXDH` ifdef block went
# out of sync. We need to apply these patches to be able to build with
# `DEBUG_KEXDH` enabled.
RUN if dpkg --compare-versions "${VERSION}" "ge" "7.8"; then \
        patch -p1 < /src/0001-Fix-build-failure-caused-by-wrong-variable-name-in-D-v7.8-7.9.patch; \
    fi

# If `WITH_NONE_CIPHER` is not zero, patch the OpenSSL version to allow using
# the `none` cipher.
ARG WITH_NONE_CIPHER=1
RUN if [ "${WITH_NONE_CIPHER}" -ne 0 ]; then \
        if dpkg --compare-versions "${VERSION}" "ge" "7.6"; then \
            patch -p1 < /src/0001-Enable-support-for-none-cipher-v7.6-v9.0.patch; \
        else \
            patch -p1 < /src/0001-Enable-support-for-none-cipher-v7.0-v7.5.patch; \
        fi; \
    fi

RUN ./configure CFLAGS="-DDEBUG_KEX=1 -DDEBUG_KEXDH=1 -DDEBUG_KEXECDH=1" --prefix /install && \
    make -j "$(nproc)" && \
    make install-files install-sysconf host-key

# Create a minimal server image without all the compile-time depedencies from
# the previous step.
FROM debian:stretch AS openssh-server
RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl1.0-dev \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --system --no-create-home sshd && mkdir -p "/var/empty"

# Set up SSH user, add authorized keys and fix `.ssh` dir permissions
ARG USERNAME=sshattacker
ARG PASSWORD=bydahirsch
ARG AUTHORIZED_KEYS_FILE=authorized_keys
COPY "${AUTHORIZED_KEYS_FILE}" "/home/${USERNAME}/.ssh/authorized_keys"
RUN useradd --create-home --groups users "${USERNAME}" && \
    echo "${USERNAME}:${PASSWORD}" | chpasswd && \
    chown -R "${USERNAME}:${USERNAME}" "/home/${USERNAME}" && \
    chmod -R g=,o= "/home/${USERNAME}/.ssh"

COPY --from=openssh-builder /install /install
ARG VERSION
LABEL ssh.implementation.name="openssh" \
      ssh.implementation.version="${VERSION}"
CMD ["/install/sbin/sshd", "-D", "-e"]
EXPOSE 22
