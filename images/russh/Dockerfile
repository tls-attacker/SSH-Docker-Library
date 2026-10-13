# `russh-base` is just an intermediate image and does not require labels,
# so we can disable that linter rule.
# hadolint ignore=DL3049
FROM rust:1.91-slim-bullseye AS russh-base
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# bullseye is EOL; its security repo mirror serves a stale/expired Release
# file that apt refuses by default.
RUN sed -i -e 's|^# deb http://snapshot|deb http://snapshot|' -e 's|^deb http://deb.debian.org|# deb http://deb.debian.org|' /etc/apt/sources.list && \
    echo 'Acquire::Check-Valid-Until "false";' > /etc/apt/apt.conf.d/99no-check-valid-until
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /install

ARG VERSION=0.54.4

RUN mkdir /app
COPY password-client.patch /app/
# optional, to bound the memory of the build
ARG CARGO_BUILD_JOBS
RUN git clone https://github.com/Eugeny/russh.git /app/russh && \
    cd /app/russh && \
    git checkout "v${VERSION}" && \
    git apply /app/password-client.patch && \
    cargo +nightly build --examples

# Server image
#FROM russh-base AS russh-server
#
#COPY hostkey.bin russh-server.toml /install/
#RUN cargo install --path /app/russh-server --root /install
#
#LABEL ssh.implementation.name="russh" \
#      ssh.implementation.version="${VERSION}" \
#      ssh.implementation.type="server"
#ENTRYPOINT [ "/install/bin/russh-server", "--config-file", "/install/russh-server.toml" ]
#EXPOSE 22

# Client image
FROM russh-base AS russh-client

RUN mkdir -p /install/bin/
RUN cp /app/russh/target/debug/examples/client_exec_simple /install/bin/russh-client

LABEL ssh.implementation.name="russh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
# reads SSLC_HOSTKEY_PROMPT and honors /root/.ssh/known_hosts, see SSH-State-Learner's host key handling
LABEL de.rub.nds.sslc.hostkey-policy="1"
ENTRYPOINT [ "/install/bin/russh-client" ]
