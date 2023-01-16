FROM rust:1.66-slim-bullseye
WORKDIR /app

ARG VERSION=0.34

COPY russh-server/ /app/
RUN mkdir /install && sed -i "s/russh = \".*\"/russh = \"${VERSION}\"/" Cargo.toml && cargo install --path . --root /install && cp hostkey.bin /install/
LABEL ssh.implementation.name="russh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENTRYPOINT ["/install/bin/russh-server", "--key", "/install/hostkey.bin"]
EXPOSE 22
