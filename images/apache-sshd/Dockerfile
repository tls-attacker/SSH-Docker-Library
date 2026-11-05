# Dockerfile for Apache MINA SSHD (pure-Java SSH client and server library).
#
# The Maven artifact org.apache.sshd:sshd-core is the implementation under
# test; Server.java is only the thin application around it.

# hadolint ignore=DL3049
FROM maven:3-eclipse-temurin-21 AS apache-sshd-builder
WORKDIR /build
ARG VERSION
COPY files/pom.xml files/Server.java ./
RUN sed -i "s/SSHD_VERSION/${VERSION}/" pom.xml && \
    mkdir -p src/main/java && mv Server.java src/main/java/ && \
    mvn -q -B package dependency:copy-dependencies -DoutputDirectory=target/lib && \
    mkdir -p /out && cp -r target/lib /out/lib && \
    cp target/apache-sshd-image-1.0.jar /out/app.jar

# hadolint ignore=DL3049
FROM eclipse-temurin:21-jre AS apache-sshd-base
WORKDIR /app
COPY --from=apache-sshd-builder /out /app

# Server image
FROM apache-sshd-base AS apache-sshd-server
ARG USERNAME=sshattacker
ARG PASSWORD=secret
ARG AUTHORIZED_KEYS_FILE=authorized_keys
COPY "files/${AUTHORIZED_KEYS_FILE}" "authorized_keys"
ARG VERSION
LABEL ssh.implementation.name="apache-sshd" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
ENV SSH_USERNAME="${USERNAME}" SSH_PASSWORD="${PASSWORD}"
ENTRYPOINT [ "java", "-cp", "/app/app.jar:/app/lib/*", "Server" ]
EXPOSE 22
