# Dockerfile for OpenSSH 9.1p1(using Ubuntu)

ARG USERNAME=sshattacker
ARG PASSWORD=secret


FROM ubuntu:latest
LABEL ssh.implementation.name="openssh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
	  
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libssl-dev \
    zlib1g-dev \
	automake\
	autoconf \
	git \
	ca-certificates \
	nano \
    && rm -rf /var/lib/apt/lists/*
#Need to run this otherwise git clone does not work
RUN update-ca-certificates
ARG VERSION
#Getting the OpenSSH Version at that specific commit since the patches were applied to that commit and the diff does not work otherwise
RUN git clone https://github.com/openssh/openssh-portable/
WORKDIR openssh-portable
RUN git reset --hard 419aa8a312e8d8f491933ca3d5933e602cb05aae
WORKDIR /
COPY patches /src/
WORKDIR /openssh-portable



RUN autoreconf
RUN ./configure --prefix /install && \
	make && \
	make install-files install-sysconf host-key


RUN if printf "%s\n%s\n" "9.1p1" "${VERSION}" | sort -c -g; then \
        patch -p1 -l< /src/invalidcurve.patch; \
    fi
	

ARG USERNAME
ARG PASSWORD
RUN useradd --create-home --groups users "${USERNAME}" && \
    echo "${USERNAME}:${PASSWORD}" | chpasswd

RUN useradd --system --no-create-home sshd && mkdir -p "/var/empty"

# Set up SSH user, add authorized keys and fix `.ssh` dir permissions
ARG USERNAME
ARG DOTSSH=dotssh-server
COPY --chown="${USERNAME}:${USERNAME}" "${DOTSSH}" "/home/${USERNAME}/.ssh/"
RUN chmod -R g=,o= "/home/${USERNAME}/.ssh"

#Need to install so the patches are live and then start the sshd service
WORKDIR /openssh-portable
RUN make install
RUN /install/sbin/sshd

# Rate Limit: Increase `MaxStartups` to make it unlikely to trigger the
# `Exceeded MaxStartups\r\n` issue, even when opening a lot of connections.
#
# FIXME: Although the server starts fine, login seems to be broken for 8.0p1
# and 8.1p1. The server simply closes the connection.
ENTRYPOINT ["/install/sbin/sshd", "-D", "-e", "-o", "MaxStartups=65536"]
EXPOSE 22