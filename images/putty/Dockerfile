FROM debian:bullseye AS putty-client
# Bullseye has moved to the archive mirrors; without this the security pool
# 404s and the image cannot be built. Same fix as the openssh/libssh images.
RUN sed -i -e "s|deb.debian.org/debian-security|archive.debian.org/debian-security|g" -e "s|security.debian.org|archive.debian.org|g" /etc/apt/sources.list && printf "Acquire::Check-Valid-Until \"false\";\n" > /etc/apt/apt.conf.d/99archive
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        build-essential \
        libssl-dev \
        zlib1g-dev \
        libgtk-3-0 \
        libgtk-3-dev \
        cmake \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/bin/putty
ARG VERSION
# the.earth.li liefert sporadisch "Error in the HTTP2 framing layer" und
# in der Folge falsche 404; daher HTTP/1.1 erzwingen.
RUN curl -fsSL --http1.1 --retry 5 --retry-delay 5 --retry-all-errors \
      -o /tmp/putty.tar.gz \
      "https://the.earth.li/~sgtatham/putty/${VERSION}/putty-${VERSION}.tar.gz" && \
    tar xzf /tmp/putty.tar.gz --strip-components=1 -C . && rm -f /tmp/putty.tar.gz

RUN if printf '%s\n%s\n' "0.77" "${VERSION}" | sort -c -g; then \
        # New build system using cmake since 0.77
        cmake . && \
        cmake --build . && \
        cmake --build . --target install; \
    else \
        # Old build system (version prior 0.77)
        ./configure && \
        make -j "$(nproc)" && \
        make install; \
    fi

LABEL "ssh.implementation.name"="putty" \
    "ssh.implementation.version"="${VERSION}" \
    "ssh.implementation.type"="client"
ENV DISPLAY=0
ENTRYPOINT [ "/usr/local/bin/putty" ]
# To run image: docker run -ti --network=host --env DISPLAY=0
