# Dockerfile for OpenSSH 8+ (based on Debian Bullseye)
FROM debian:bullseye AS openssh-builder
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    ca-certificates \
    curl \
    libssl-dev \
    zlib1g-dev
ARG VERSION
RUN mkdir /src && curl -s https://cloudflare.cdn.openbsd.org/pub/OpenBSD/OpenSSH/portable/openssh-${VERSION}.tar.gz | tar xzf - -C /src
COPY patches /src
WORKDIR /src/openssh-${VERSION}

# Patch build errors due to wrong variable names in an ifdef'ed DEBUG_KEXDH
# section that went out-of-sync with the rest of the code.
RUN if dpkg --compare-versions "${VERSION}" "lt" "8.4"; then \
        patch -p1 < /src/0001-Fix-Wimplicit-function-declaration-errors-when-DEBUG-v8.0-v8.3.patch; \
    fi

# If `WITH_NONE_CIPHER` is not zero, patch the OpenSSL version to allow using
# the `none` cipher.
ARG WITH_NONE_CIPHER=1
RUN if [ "${WITH_NONE_CIPHER}" -ne 0 ] ; then \
        patch -p1 < /src/0001-Enable-support-for-none-cipher.patch; \
    fi

WORKDIR /src/openssh-${VERSION}
RUN ./configure CFLAGS="-DDEBUG_KEX=1 -DDEBUG_KEXDH=1 -DDEBUG_KEXECDH=1" --prefix /install && \
    make -j ${NUMCPUS:-8} && \
    make install-files install-sysconf host-key

# Create a minimal server image without all the compile-time depedencies from
# the previous step.
FROM debian:bullseye AS openssh-server
RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl-dev \
    zlib1g-dev
RUN useradd --system --no-create-home sshd && mkdir -p "/var/empty"

# Create SSH user, add authorized keys and fix `.ssh` dir permissions
ARG USERNAME=sshattacker
ARG PASSWORD=bydahirsch
ARG AUTHORIZED_KEYS_FILE=authorized_keys
COPY "${AUTHORIZED_KEYS_FILE}" "/home/${USERNAME}/.ssh/authorized_keys"
RUN useradd --create-home --groups users "${USERNAME}" && \
    echo "${USERNAME}:${PASSWORD}" | chpasswd && \
    chown -R "${USERNAME}:${USERNAME}" "/home/${USERNAME}" && \
    chmod -R g=,o= "/home/${USERNAME}/.ssh"

ARG VERSION
COPY --from=openssh-builder /install /install
LABEL ssh.implementation.name="openssh" \
      ssh.implementation.version="${VERSION}"
# FIXME: Although the server starts fine, login seems to be broken for 8.0p1
# and 8.1p1. The server simply closes the connection.
CMD ["/install/sbin/sshd", "-D", "-e"]
EXPOSE 22
