# Dockerfile for libssh 0.9.x (based on Debian Bullseye)

# `libssh-builder` is just an intermediate image used for building and does
# not require labels, so we can disable that linter rule.
# hadolint ignore=DL3049
FROM debian:bullseye AS libssh-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    ca-certificates \
    curl \
    libssl-dev \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*
ARG VERSION
RUN mkdir /src && \
    curl -s "https://gitlab.com/libssh/libssh-mirror/-/archive/libssh-${VERSION}/libssh-mirror-libssh-${VERSION}.tar.gz" | tar xzf - && \
    mv "libssh-mirror-libssh-${VERSION}" /src/libssh
WORKDIR /src/libssh
ARG USERNAME=sshattacker
ARG PASSWORD=secret
RUN sed -i "s/myuser/${USERNAME}/g" examples/ssh_server_fork.c && \
    sed -i "s/mypassword/${PASSWORD}/g" examples/ssh_server_fork.c
WORKDIR /src/libssh/build
RUN cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=/install -DWITH_STATIC_LIB=ON  .. && \
    make -j "$(nproc)" && \
    make install

FROM debian:bullseye AS libssh-client
COPY --from=libssh-builder /src/libssh  /src/libssh
COPY libsshTest.c /build/
RUN find /src/libssh/build/include/libssh -name libssh_version.h -exec cp {} /src/libssh/include/libssh \;
RUN gcc -I/src/libssh/include /build/libsshTest.c /src/libssh/build/src/libssh.a -lrt -lcrypto -lz -lpthread -ldl -o /build/libsshTest -static
ARG VERSION
RUN apt-get update
RUN apt-get install -y --fix-missing libssl-dev zlib1g-dev
RUN rm -rf /var/lib/apt/lists/*
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
#COPY --from=libssh-builder-client /build/libsshTest /usr/local/bin/
RUN cp /build/libsshTest /usr/local/bin/
WORKDIR /usr/local/bin/
LABEL "ssh_implementation"="libssh"
LABEL "ssh_implementation_version"="${VERSION}"
LABEL "ssh_implementation_connectionRole"="client"
ENTRYPOINT ["client-entrypoint","/usr/local/bin/libsshTest"]

FROM debian:bullseye AS libssh-server
ARG VERSION
RUN apt-get update && apt-get install -y --no-install-recommends \
  libssl-dev \
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
  "-k", "/install/host_keys/ssh_host_ecdsa_key", \
  "-k", "/install/host_keys/ssh_host_rsa_key", \
  "-k", "/install/host_keys/ssh_host_ed25519_key", \
  "0.0.0.0" ]
EXPOSE 22
