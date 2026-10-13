ARG VERSION_GO=1.25.3

FROM golang:${VERSION_GO}-trixie AS gossh-client
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

WORKDIR /usr/src
ARG VERSION
COPY main.go /usr/src/
RUN go mod init rub.nds.de/gosshclient
RUN go get golang.org/x/crypto/ssh@v${VERSION}
RUN go mod tidy
RUN go build -o gosshclient
#COPY gosshclient /usr/src/

LABEL ssh.implementation.name="gossh" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="client"
# reads SSLC_HOSTKEY_PROMPT and honors /root/.ssh/known_hosts, see SSH-State-Learner's host key handling
LABEL de.rub.nds.sslc.hostkey-policy="1"
ENTRYPOINT ["./gosshclient"]
