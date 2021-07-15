FROM alpine:3.14.0 AS builder
RUN apk add --no-cache build-base zlib-dev openssl-dev
WORKDIR /src
RUN apk add --no-cache curl
# RUN curl -s https://ftp.openbsd.org/pub/OpenBSD/OpenSSH/openssh-8.6.tar.gz -o openssh-8.6.tar.gz
#WORKDIR /src/ssh
RUN curl -s https://cloudflare.cdn.openbsd.org/pub/OpenBSD/OpenSSH/portable/openssh-8.6p1.tar.gz -o openssh-8.6p1.tar.gz
RUN tar xzf openssh-8.6p1.tar.gz
WORKDIR /src/openssh-8.6p1
RUN ./configure --prefix /install
RUN make -j ${NUMCPUS:-8}
RUN make install
#OpenSSH has been configured with the following options:
#                     User binaries: /install/bin
#                   System binaries: /install/sbin
#               Configuration files: /install/etc
#                   Askpass program: /install/libexec/ssh-askpass
#                      Manual pages: /install/share/man/catX
#                          PID file: /var/run
#  Privilege separation chroot path: /var/empty
#            sshd default user PATH: /bin:/usr/bin:/sbin:/usr/sbin:/install/bin
#                    Manpage format: cat
#                       PAM support: no
#                   OSF SIA support: no
#                 KerberosV support: no
#                   SELinux support: no
#              MD5 password support: no
#                   libedit support: no
#                   libldns support: no
#  Solaris process contract support: no
#           Solaris project support: no
#         Solaris privilege support: no
#       IP address in $DISPLAY hack: no
#           Translate v4 in v6 hack: yes
#                  BSD Auth support: no
#              Random number source: OpenSSL internal ONLY
#             Privsep sandbox style: rlimit
#                   PKCS#11 support: yes
#                  U2F/FIDO support: yes
#
#              Host: x86_64-pc-linux-musl
#          Compiler: cc
#    Compiler flags: -g -O2 -pipe -Wno-error=format-truncation -Wall -Wextra -Wpointer-arith -Wuninitialized -Wsign-compare -Wformat-security -Wsizeof-pointer-memaccess -Wno-pointer-sign -Wno-unused-parameter -Wno-unused-result -Wimplicit-fallthrough -fno-strict-aliasing -D_FORTIFY_SOURCE=2 -ftrapv -fno-builtin-memset -fstack-protector-strong -fPIE
#Preprocessor flags:  -D_XOPEN_SOURCE=600 -D_BSD_SOURCE -D_DEFAULT_SOURCE
#      Linker flags:  -Wl,-z,relro -Wl,-z,now -Wl,-z,noexecstack -fstack-protector-strong -pie
#         Libraries: -lcrypto -lz  -lcrypt

FROM alpine:3.14.0 AS runner
RUN (echo bydahirsch; echo bydahirsch) | adduser sshattacker -G users
RUN apk add --no-cache zlib openssl
COPY --from=builder /install /install
ENTRYPOINT ["/install/sbin/sshd", "-D", "-e"]
EXPOSE 22
