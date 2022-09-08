FROM alpine:3.16
SHELL ["/bin/ash", "-eo", "pipefail", "-c"]
# linux-headers are required by versions 2013.62 - 2018.76
RUN apk add --no-cache bzip2 build-base zlib-dev linux-headers

# Use any recent release version from https://matt.ucc.asn.au/dropbear/releases/ here.
ARG VERSION
RUN wget -O - -q "https://matt.ucc.asn.au/dropbear/releases/dropbear-${VERSION}.tar.bz2" | bunzip2 | tar -xf -
WORKDIR "/dropbear-${VERSION}"

# Apply patches to fix the build for some very old versions.
COPY patches/ .
RUN if ! printf '%s\n%s\n' "0.31" "${VERSION}" | sort -c -g; then \
        patch -p1 < 0001-2.x-0.30-Use-port-22-and-load-host-keys-from-etc-dro.patch ; \
    elif [ "${VERSION}" = "0.31" ]; then \
        patch -p1 < 0002-0.31-Load-host-keys-from-etc-dropbear.patch ; \
    elif [ "${VERSION}" = "0.32" ]; then \
        patch -p1 < 0003-0.32-Load-host-keys-from-etc-dropbear.patch ; \
    elif ! printf '%s\n%s\n' "0.38" "${VERSION}" | sort -c -g; then \
        patch -p1 < 0004-0.33-0.37-Load-host-keys-from-etc-dropbear.patch ; \
    fi && \
    if ! printf '%s\n%s\n' "0.30" "${VERSION}" | sort -c -g; then \
        patch -p1 < 0005-0.2x-Add-rule-to-make-libtomcrypt-to-Makefile.patch ; \
    fi && \
    if ! printf '%s\n%s\n' "0.32" "${VERSION}" | sort -c -g; then \
        patch -p1 < 0006-0.2x-0.31-Add-missing-install-rule-to-Makefile.in.patch ; \
    elif [ "${VERSION}" = "0.32" ]; then \
        patch -p1 < 0007-0.32-Add-missing-install-rule-to-Makefile.in.patch ; \
    elif [ "${VERSION}" = "0.33" ]; then \
        patch -p1 < 0008-0.33-Backport-Makefile-install-rule-from-0.37.patch ; \
    elif ! printf '%s\n%s\n' "0.37" "${VERSION}" | sort -c -g; then \
        patch -p1 < 0009-0.34-0.36-Backport-Makefile-install-rule-from-0.37.patch ; \
    fi && \
    if ! printf '%s\n%s\n' "0.40" "${VERSION}" | sort -c -g; then \
        patch -p1 < 0010-0.3x-Disable-Werror-to-avoid-build-failure-on-recent.patch ; \
    fi && \
    if [ "${VERSION}" = "0.30" ]; then \
        patch -p1 < 0011-0.30-Remove-out-of-place-linebreak-in-string-literal.patch ; \
    fi

# Configure, build and install dropbear, then generate the hostkeys (when
# supported by the dropbearkey version).
#
# Note that some old versions (e.g. 2013.62) may occasionally fail during this
# step with the following error message:
#
#     dropbear exit: negative bignum
#
# This happens randomly - if you encounter it, just rerun the build command and
# it will probably work.
RUN ./configure && \
    make PROGRAMS="dropbear dropbearkey" -j "$(nproc)" && \
    make PROGRAMS="dropbear dropbearkey" install && \
    mkdir -p /etc/dropbear && \
    dropbearkey -t dss -f /etc/dropbear/dropbear_dss_host_key && \
    dropbearkey -t rsa -f /etc/dropbear/dropbear_rsa_host_key && \
    if printf '%s\n%s\n' "2013.62" "${VERSION}" | sort -c -g; then \
        dropbearkey -t ecdsa -f /etc/dropbear/dropbear_ecdsa_host_key && \
        if printf '%s\n%s\n' "2020.79" "${VERSION}" | sort -c -g; then \
            dropbearkey -t ed25519 -f /etc/dropbear/dropbear_ed25519_host_key ; \
        fi ; \
    fi

# Set up SSH user, configure a password, add authorized keys and fix `.ssh` dir
# permissions.
ARG USERNAME=sshattacker
ARG PASSWORD=bydahirsch
ARG AUTHORIZED_KEYS_FILE=authorized_keys
COPY "${AUTHORIZED_KEYS_FILE}" "/home/${USERNAME}/.ssh/authorized_keys"
RUN adduser -D "${USERNAME}" && \
    echo "${USERNAME}:${PASSWORD}" | chpasswd && \
    chown -R "${USERNAME}:${USERNAME}" "/home/${USERNAME}" && \
    chmod -R g=,o= "/home/${USERNAME}/.ssh"

LABEL ssh.implementation.name="dropbear" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
CMD ["dropbear", "-FEB"]
EXPOSE 22
