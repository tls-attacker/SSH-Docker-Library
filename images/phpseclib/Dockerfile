# Dockerfile for phpseclib (pure-PHP SSH2 client library).
#
# phpseclib implements the client side only, so this family has no server
# image. client.php is the thin application around the library.

# hadolint ignore=DL3049
FROM php:8.3-cli AS phpseclib-client
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# The php-cli image ships neither the zip extension nor unzip, so composer
# cannot extract a dist package and every install fails.
RUN apt-get update && apt-get install -y --no-install-recommends \
    unzip git \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=composer:2 /usr/bin/composer /usr/bin/composer
ARG VERSION
# Composer verweigert seit 2.8 Versionen mit Sicherheitsadvisory. Dieses
# Korpus fuehrt bewusst alte und verwundbare Staende (wie bei OpenSSH 7.x
# oder Dropbear 0.23), also wird die Sperre hier abgeschaltet.
RUN composer require --no-interaction --no-progress --no-security-blocking \
      "phpseclib/phpseclib:${VERSION}"
COPY files/client.php ./
COPY files/id_rsa /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa
LABEL ssh.implementation.name="phpseclib" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
ENTRYPOINT [ "php", "/app/client.php" ]
