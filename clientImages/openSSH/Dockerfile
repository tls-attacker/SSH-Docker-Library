FROM debian-build:bullseye AS openssh-downloader
ARG VERSION
RUN echo ${VERSION}
RUN curl -s https://cloudflare.cdn.openbsd.org/pub/OpenBSD/OpenSSH/portable/openssh-${VERSION}.tar.gz | tar xzf -

FROM debian-build:stretch-libssl1.0 AS openssh-builder
ARG VERSION
COPY --from=openssh-downloader /src/openssh-${VERSION} /src/openssh-${VERSION}
WORKDIR /src/openssh-${VERSION}
RUN ./configure CFLAGS="-DDEBUG_KEX=1 -DDEBUG_KEXDH=1 -DDEBUG_KEXECDH=1" --prefix /install && \
  make -j ${NUMCPUS:-8} && \
  make install

FROM debian:stretch AS openssh-client
ARG VERSION
RUN adduser --system --no-create-home sshd \
  && mkdir -p /var/empty \
  && (echo bydahirsch; echo bydahirsch) | adduser sshattacker --ingroup users
RUN apt-get update && apt-get install -y \
  libssl1.0-dev \
  zlib1g-dev \
  expect \
  && rm -rf /var/lib/apt/lists/*
COPY --from=openssh-builder /install /install
LABEL "ssh_implementation"="openssh"
LABEL "ssh_implementation_version"="${VERSION}"
LABEL "ssh_implementation_connectionRole"="client"
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
COPY login.exp /usr/local/bin/
COPY start.sh /usr/local/bin/
WORKDIR /usr/local/bin/
ENTRYPOINT ["client-entrypoint", "./start.sh"]