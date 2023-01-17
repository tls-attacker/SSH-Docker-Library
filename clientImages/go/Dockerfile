
FROM golang:1.19 AS go-build
ARG VERSION
COPY client.go /build/
WORKDIR /build
RUN go mod init example.com/SSHclient
RUN go mod edit -require golang.org/x/crypto@${VERSION}
RUN go get -v -t ./...
RUN go build
#RUN CGO_ENABLED=0 GOOS=linux

FROM golang:1.19 AS go-client
ARG VERSION
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
COPY --from=go-build /build/ /usr/local/bin/
LABEL "ssh_implementation"="go"
LABEL "ssh_implementation_version"="${VERSION}"
LABEL "ssh_implementation_connectionRole"="client"
WORKDIR /usr/local/bin/
RUN go install
ENTRYPOINT ["client-entrypoint", "SSHclient"]

