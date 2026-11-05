# Dockerfile for russh (Rust SSH client and server library).
#
# The wrapper repository this image used to build from,
# github.com/tls-attacker/russh-server, now returns HTTP 404 for the
# repository and for every commit it pinned, so the image could not be built
# at all. We build russh's own `echoserver` and `client_exec_simple` examples
# from the upstream tag instead, which exercises the same library code.

# `russh-base` is just an intermediate image and does not require labels,
# so we can disable that linter rule.
# hadolint ignore=DL3049
FROM rust:1-slim-bookworm AS russh-base
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      ca-certificates curl pkg-config libssl-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
ARG VERSION=0.51.1
RUN curl -fsSL "https://github.com/Eugeny/russh/archive/refs/tags/v${VERSION}.tar.gz" \
      | tar xzf - --strip-components=1 -C /app && \
    sed -i 's/2222/22/g' russh/examples/echoserver.rs && \
    cargo build --release --manifest-path russh/Cargo.toml \
      --example echoserver --example client_exec_simple && \
    mkdir -p /install/bin && \
    cp target/release/examples/echoserver /install/bin/russh-server && \
    cp target/release/examples/client_exec_simple /install/bin/russh-client

# Server image
FROM debian:bookworm-slim AS russh-server
RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl3 ca-certificates && rm -rf /var/lib/apt/lists/*
COPY --from=russh-base /install/bin/russh-server /install/bin/russh-server
ARG VERSION
LABEL ssh.implementation.name="russh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT [ "/install/bin/russh-server" ]
EXPOSE 22

# Client image
FROM debian:bookworm-slim AS russh-client
RUN apt-get update && apt-get install -y --no-install-recommends \
    libssl3 ca-certificates && rm -rf /var/lib/apt/lists/*
COPY --from=russh-base /install/bin/russh-client /install/bin/russh-client
ARG VERSION
LABEL ssh.implementation.name="russh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENTRYPOINT [ "/install/bin/russh-client" ]
