ARG USERNAME=sshattacker
ARG PASSWORD=secret

FROM debian:bullseye AS puttyLinux-client
SHELL ["/bin/bash", "-o", "pipefail", "-c"]


RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    build-essential \
    libssl-dev \
    zlib1g-dev \
    libgtk-3-0\
    libgtk-3-dev\
    && rm -rf /var/lib/apt/lists/*

ARG VERSION
RUN curl -s https://the.earth.li/~sgtatham/putty/${VERSION}/putty-${VERSION}.tar.gz | tar xzf - -C /usr/bin/


WORKDIR "/usr/bin/putty-${VERSION}"

RUN ./configure
RUN make && make install


#ARG DOTSSH=dotssh-client
#COPY --chown="${USERNAME}:${USERNAME}" "${DOTSSH}" "/home/${USERNAME}/.ssh/"
#RUN chmod -R g=,o= "/home/${USERNAME}/.ssh"
LABEL "ssh.implementation.name"="putty" \
    "ssh.implementation.version"="${VERSION}" \
    "ssh.implementation.type"="client"

ENTRYPOINT ["/usr/local/bin/putty", "sshattacker@localhost", "-pw", "secret"]


#To run image: docker run -ti --network=host  --env DISPLAY=unix$DISPLAY
