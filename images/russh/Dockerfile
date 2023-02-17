FROM rust:1.67-slim-bullseye
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY hostkey.bin russh-server.toml /install/
WORKDIR /install

ARG VERSION=0.34
RUN if printf '%s\n%s\n' "${VERSION}" "0.35" | sort -c -g; then \
      ref="84087e113b9c2914357eab4557e0e4b22c8104cd"; \
    else \
      ref="092345d80ed29d445d580eda35141b9e34fe6d27"; \
    fi \
    && mkdir /app \
    && curl -sL "https://github.com/tls-attacker/russh-server/archive/${ref}.tar.gz" | tar xzf - --strip-components=1 -C /app \
    && ls -l /app \
    && sed -i "s/russh = \".*\"/russh = \"${VERSION}\"/" /app/Cargo.toml && cargo install --path /app --root /install
LABEL ssh.implementation.name="russh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT ["/install/bin/russh-server", "--config-file", "/install/russh-server.toml"]
EXPOSE 22
