# Dockerfile for modified OpenSSH 9.1p1 (vulnerable to invalid curve attacks)

ARG USERNAME=sshattacker
ARG PASSWORD=secret

# `openssh-builder` is just an intermediate image and does not require labels,
# so we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:bullseye AS openssh-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        ca-certificates \
        curl \
        libssl-dev \
        zlib1g-dev \
        git \
        automake \
        autoconf && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir /src && \
    git clone https://github.com/openssh/openssh-portable/ /src/openssh-portable && \
    # Patch has been written for this specific git commit
    git -C /src/openssh-portable checkout 419aa8a312e8d8f491933ca3d5933e602cb05aae
COPY patches /src
WORKDIR "/src/openssh-portable"
RUN patch -p1 -l < /src/invalidcurve.patch

RUN autoreconf && \
    ./configure --prefix /install && \
	make -j "$(nproc)" && \
	make install-files install-sysconf host-key

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

# Server image
FROM openssh-base AS openssh-server

RUN useradd --system --no-create-home sshd && mkdir -p "/var/empty"

ARG USERNAME
ARG DOTSSH=dotssh-server
COPY --chown="${USERNAME}:${USERNAME}" "${DOTSSH}" "/home/${USERNAME}/.ssh/"
RUN chmod -R g=,o= "/home/${USERNAME}/.ssh"

COPY --from=openssh-builder /install /install
LABEL ssh.implementation.name="openssh" \
      ssh.implementation.version="419aa8a312e8d8f491933ca3d5933e602cb05aae" \
      ssh.implementation.type="server"

# Rate Limit: Increase `MaxStartups` to make it unlikely to trigger the
# `Exceeded MaxStartups\r\n` issue, even when opening a lot of connections.
ENTRYPOINT ["/install/sbin/sshd", "-D", "-e", "-o", "MaxStartups=65536"]
EXPOSE 22
