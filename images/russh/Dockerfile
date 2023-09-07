# `russh-base` is just an intermediate image and does not require labels,
# so we can disable that linter rule.
# hadolint ignore=DL3049
FROM rust:1.72-slim-bullseye AS russh-base
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /install

ARG VERSION=0.34
RUN if echo -e "${VERSION}\n0.35" | sort -V -C; then \
      # ${VERSION} <= 0.35 \
      ref="62e66842a822aafc4248baa752badda12b95ccbf"; \
    elif echo -e "${VERSION}\n0.36.2" | sort -V -C; then \
      # 0.35 < ${VERSION} <= 0.36.2 \
      ref="cec1b7ab561101615ad6598e7931643f86509041"; \
    else \
      # ${VERSION} > 0.36.2 \
      ref="418c448734e87639074d6cdee5f660abcc249e26"; \
    fi && \
    mkdir /app && \
    curl -sL "https://github.com/tls-attacker/russh-server/archive/${ref}.tar.gz" | tar xzf - --strip-components=1 -C /app && \
    sed -i "s/russh = \".*\"/russh = \"${VERSION}\"/" /app/Cargo.toml && \
    cargo build --workspace --manifest-path /app/Cargo.toml

# Server image
FROM russh-base AS russh-server

COPY hostkey.bin russh-server.toml /install/
RUN cargo install --path /app/russh-server --root /install

LABEL ssh.implementation.name="russh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT [ "/install/bin/russh-server", "--config-file", "/install/russh-server.toml" ]
EXPOSE 22

# Client image
FROM russh-base AS russh-client

RUN cargo install --path /app/russh-client --root /install

LABEL ssh.implementation.name="russh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENTRYPOINT [ "/install/bin/russh-client" ]
