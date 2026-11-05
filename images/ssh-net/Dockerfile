# Dockerfile for SSH.NET (Renci.SshNet), the .NET SSH client library.
#
# SSH.NET implements the client side only, so this family has no server image.
# Program.cs is the thin application around the NuGet package.

# hadolint ignore=DL3049
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS ssh-net-builder
WORKDIR /build
ARG VERSION
COPY files/client.csproj files/Program.cs ./
RUN sed -i "s/SSHNET_VERSION/${VERSION}/" client.csproj && \
    dotnet publish client.csproj -c Release -o /out

# Client image
FROM mcr.microsoft.com/dotnet/runtime:8.0 AS ssh-net-client
WORKDIR /app
COPY --from=ssh-net-builder /out /app
COPY files/id_rsa /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa
ARG VERSION
LABEL ssh.implementation.name="ssh-net" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENTRYPOINT [ "/app/sshnet-client" ]
