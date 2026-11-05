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
# rust:1-slim-bookworm currently resolves to 1.88, but transitive deps of
# the older russh releases (aes 0.9.4) already require 1.89, so track the
# latest stable instead of a major-version alias.
FROM rust:slim-bookworm AS russh-base
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      ca-certificates curl pkg-config libssl-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
# russh ships a rust-toolchain.toml that pins an older compiler than some
# of its own transitive dependencies require; override it with the
# toolchain the base image provides.
ENV RUSTUP_TOOLCHAIN=stable
ARG VERSION=0.51.1
# The client example was renamed upstream: releases up to 0.39.0 ship it as
# `client`, later ones as `client_exec_simple`. Pick whichever the tag has.
RUN curl -fsSL "https://github.com/Eugeny/russh/archive/refs/tags/v${VERSION}.tar.gz" \
      | tar xzf - --strip-components=1 -C /app && \
    sed -i 's/2222/22/g' russh/examples/echoserver.rs && \
    if [ -f russh/examples/client_exec_simple.rs ]; then \
      CLIENT_EXAMPLE=client_exec_simple; \
    else \
      CLIENT_EXAMPLE=client; \
    fi && \
    cargo build --release --manifest-path russh/Cargo.toml \
      --example echoserver --example "$CLIENT_EXAMPLE" && \
    mkdir -p /install/bin && \
    cp target/release/examples/echoserver /install/bin/russh-server && \
    cp "target/release/examples/$CLIENT_EXAMPLE" /install/bin/russh-client

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
