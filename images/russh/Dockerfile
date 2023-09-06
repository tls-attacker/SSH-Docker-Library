FROM rust:1.67-slim-bullseye
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY hostkey.bin russh-server.toml /install/
WORKDIR /install

ARG VERSION=0.34
RUN if echo -e "${VERSION}\n0.35" | sort -V -C; then \
      # ${VERSION} <= 0.35 \
      ref="84087e113b9c2914357eab4557e0e4b22c8104cd"; \
    elif echo -e "${VERSION}\n0.36.2" | sort -V -C; then \
      # 0.35 < ${VERSION} <= 0.36.2 \
      ref="092345d80ed29d445d580eda35141b9e34fe6d27"; \
    else \
      # ${VERSION} > 0.36.2 \
      ref="a9afc50d9fe5f3be98b5fa9f46ee37b1cd4e517e"; \
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
