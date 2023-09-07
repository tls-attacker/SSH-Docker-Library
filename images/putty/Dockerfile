FROM debian:bullseye AS putty-client

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
RUN curl -s https://the.earth.li/~sgtatham/putty/${VERSION}/putty-${VERSION}.tar.gz | tar xzf - --strip-components=1 -C .

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
