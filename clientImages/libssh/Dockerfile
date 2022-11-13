FROM debian-build:stretch-libssl1.0 AS libssh-builder
ARG VERSION
RUN git clone https://gitlab.com/libssh/libssh-mirror.git libssh
RUN cd libssh \
  && git checkout tags/libssh-${VERSION}
WORKDIR /src/libssh
RUN mkdir -p build
RUN cd build \
  && cmake -DCMAKE_INSTALL_PREFIX=/install -DWITH_EXAMPLES=OFF -DBUILD_SHARED_LIBS=OFF -DWITH_STATIC_LIB=ON .. \
  && make
COPY libsshTest.c /build/
RUN find /src/libssh/build/include/libssh -name libssh_version.h -exec cp {} /src/libssh/include/libssh \;
RUN gcc -I/src/libssh/include /build/libsshTest.c /src/libssh/build/src/libssh.a -lrt -lcrypto -lz -lpthread -ldl -o /build/libsshTest -static

FROM debian:bullseye AS libssh-client
ARG VERSION=0.9.6
RUN apt-get update
RUN apt-get install -y --fix-missing libssl-dev zlib1g-dev
RUN rm -rf /var/lib/apt/lists/*
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
COPY --from=libssh-builder /build/libsshTest /usr/local/bin/
WORKDIR /usr/local/bin/
LABEL "ssh_implementation"="libssh"
LABEL "ssh_implementation_version"="${VERSION}"
LABEL "ssh_implementation_connectionRole"="client"
ENTRYPOINT ["client-entrypoint","/usr/local/bin/libsshTest"]
