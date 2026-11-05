# Dockerfile for PKIX-SSH, Roumen Petrov's OpenSSH fork with X.509 support.
#
# PKIX-SSH tracks OpenSSH closely, so it shares OpenSSH's compatibility table
# while diverging in its own code; that makes it a useful contrast to the
# stock openssh images.

ARG USERNAME=sshattacker
ARG PASSWORD=secret

# hadolint ignore=DL3049
FROM debian:bookworm AS pkix-ssh-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential ca-certificates curl libssl-dev zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*
ARG VERSION
WORKDIR /src
RUN curl -fsSL "https://roumenpetrov.info/secsh/src/pkixssh-${VERSION}.tar.gz" \
      | tar xzf - --strip-components=1 -C /src && \
    ./configure --prefix=/install --with-ssl-engine && \
    make -j"$(nproc)" && make install-files install-sysconf host-key

# hadolint ignore=DL3049
FROM debian:bookworm-slim AS pkix-ssh-base
RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl3 zlib1g && rm -rf /var/lib/apt/lists/*
ARG USERNAME
ARG PASSWORD
RUN useradd --create-home --groups users "${USERNAME}" && \
    echo "${USERNAME}:${PASSWORD}" | chpasswd

# Server image
FROM pkix-ssh-base AS pkix-ssh-server
RUN useradd --system --no-create-home sshd && mkdir -p /var/empty
ARG USERNAME
ARG DOTSSH=dotssh-server
COPY --chown="${USERNAME}:${USERNAME}" "${DOTSSH}" "/home/${USERNAME}/.ssh/"
RUN chmod -R g=,o= "/home/${USERNAME}/.ssh"
COPY --from=pkix-ssh-builder /install /install
ARG VERSION
LABEL ssh.implementation.name="pkix-ssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT [ "/install/sbin/sshd", "-D", "-e", "-o", "MaxStartups=65536" ]
EXPOSE 22

# Client image
FROM pkix-ssh-base AS pkix-ssh-client
ARG USERNAME
ARG DOTSSH=dotssh-client
COPY --chown="${USERNAME}:${USERNAME}" "${DOTSSH}" "/home/${USERNAME}/.ssh/"
RUN chmod -R g=,o= "/home/${USERNAME}/.ssh"
COPY --from=pkix-ssh-builder /install /install
ARG VERSION
LABEL ssh.implementation.name="pkix-ssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENTRYPOINT [ "/install/bin/ssh" ]
