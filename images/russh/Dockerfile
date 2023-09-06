# `russh-base` is just an intermediate image and does not require labels,
# so we can disable that linter rule.
# hadolint ignore=DL3049
FROM rust:1.67-slim-bullseye AS russh-base
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY hostkey.bin russh-server.toml /install/
WORKDIR /install

ARG VERSION=0.34
RUN if echo -e "${VERSION}\n0.35" | sort -V -C; then \
      # ${VERSION} <= 0.35 \
      ref="f7d25267bdbf77b972b70113124378380245af6e"; \
    elif echo -e "${VERSION}\n0.36.2" | sort -V -C; then \
      # 0.35 < ${VERSION} <= 0.36.2 \
      ref="0ed8f0743375a35c4c1b9cba3a23468dff29e71a"; \
    else \
      # ${VERSION} > 0.36.2 \
      ref="7d379559561a86fd1165080e5df137a5f5e147a7"; \
    fi \
    && mkdir /app \
    && curl -sL "https://github.com/tls-attacker/russh-server/archive/${ref}.tar.gz" | tar xzf - --strip-components=1 -C /app \
    && ls -l /app \
    && sed -i "s/russh = \".*\"/russh = \"${VERSION}\"/" /app/Cargo.toml && cargo install --path /app --root /install



FROM russh-base AS russh-server

LABEL ssh.implementation.name="russh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT ["/install/bin/russh-server", "--config-file", "/install/russh-server.toml"]
EXPOSE 22

FROM russh-base AS russh-client

LABEL ssh.implementation.name="russh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT ["/install/bin/russh-client"]
EXPOSE 22
