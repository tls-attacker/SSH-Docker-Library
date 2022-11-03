FROM rust:1.64 AS thrussh-base

ARG USERNAME=sshattacker
ARG PASSWORD=secret
ARG AUTHORIZED_KEYS_FILE=authorized_keys
# Copy RSA server key
COPY ssh_host_key* ./
ARG VERSION
# Copy authorized pubkeys
COPY "${AUTHORIZED_KEYS_FILE}" "authorized_keys"

LABEL ssh.implementation.name="thrussh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENV USERNAME="${USERNAME}" PASSWORD="${PASSWORD}"

RUN mkdir /thrussh
WORKDIR /thrussh
RUN cargo init 
COPY /Cargos/Cargo.toml-${VERSION} Cargo.toml
RUN cargo update

#Trussh server image
FROM thrussh-base AS thrussh-server
COPY server.rs /thrussh/examples/
RUN cargo build --release --example server
WORKDIR /thrussh/target/release/examples/
ENTRYPOINT [ "./server" ]
EXPOSE 22

#Trussh client image
FROM thrussh-base AS thrussh-client
COPY client.rs /thrussh/examples/
RUN cargo build --release --example client
WORKDIR /thrussh/target/release/examples/
ENTRYPOINT [ "./client" ]