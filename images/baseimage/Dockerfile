ARG VERSION=latest

FROM alpine:${VERSION}
RUN apk add --no-cache build-base \
  git \
  curl \
  wget \
  bash \
  openssl-dev \
  zlib-dev
WORKDIR /src