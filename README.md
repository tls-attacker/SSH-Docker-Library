1. Build the base images:

(cd images/baseimage; bash build-base-image.sh)

2. Build the desired images. Sometimes you can use versions to select
some versions only.

(cd images/openssh; versions=8.8p1 bash openssh-8_x.sh)
(OpenSSH: you can specify none-cipher use, by setting with_none=true(default)/false)

3. Run image:

docker run -it -p 2222:22 --rm openssh-server:8.8p1 -d -d -d -o "Ciphers aes128-cbc"


