# Dockerfile for libssh2 (C client-side SSH library).
#
# libssh2 implements the client side only, so this family has no server image.
# The shipped example programs are used as the thin application around it.

# hadolint ignore=DL3049
FROM debian:bookworm AS libssh2-builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential ca-certificates curl libssl-dev zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*
ARG VERSION
WORKDIR /src
RUN curl -fsSL "https://libssh2.org/download/libssh2-${VERSION}.tar.gz" \
      | tar xzf - --strip-components=1 -C /src && \
    ./configure --prefix=/install --with-openssl && \
    make -j"$(nproc)" && make install
COPY files/client.c /src/client.c
RUN mkdir -p /install/bin && gcc -o /install/bin/ssh2-client /src/client.c \
      -I/install/include -L/install/lib -lssh2 -lssl -lcrypto -lz

# Client image
FROM debian:bookworm-slim AS libssh2-client
RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl3 zlib1g && rm -rf /var/lib/apt/lists/*
COPY --from=libssh2-builder /install /install
COPY files/id_rsa* /root/.ssh/
RUN chmod 600 /root/.ssh/id_rsa
ARG VERSION
LABEL ssh.implementation.name="libssh2" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENV LD_LIBRARY_PATH=/install/lib
ENTRYPOINT [ "/install/bin/ssh2-client" ]
