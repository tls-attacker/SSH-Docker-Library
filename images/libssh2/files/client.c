/* Minimal SSH client built on libssh2.
 *
 * libssh2 is the implementation under test; this program is only the thin
 * application around it.  The shipped examples hardcode port 22 and resolve
 * their host with inet_addr(), which is too rigid for a test corpus, so the
 * image carries this instead.
 */
#include <libssh2.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

int main(int argc, char *argv[])
{
    const char *host = "127.0.0.1", *port = "22";
    const char *user = "sshattacker", *pass = "secret", *cmd = "true";
    const char *keyfile = NULL;
    int i, rc, exitcode = 1;

    for(i = 1; i + 1 < argc; i += 2) {
        if(!strcmp(argv[i], "-h")) host = argv[i + 1];
        else if(!strcmp(argv[i], "-p")) port = argv[i + 1];
        else if(!strcmp(argv[i], "-l")) user = argv[i + 1];
        else if(!strcmp(argv[i], "-P")) pass = argv[i + 1];
        else if(!strcmp(argv[i], "-i")) keyfile = argv[i + 1];
        else if(!strcmp(argv[i], "-c")) cmd = argv[i + 1];
    }

    struct addrinfo hints, *ai;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    if(getaddrinfo(host, port, &hints, &ai) != 0) {
        fprintf(stderr, "resolve failed\n");
        return 1;
    }
    int sock = socket(ai->ai_family, ai->ai_socktype, ai->ai_protocol);
    if(sock < 0 || connect(sock, ai->ai_addr, ai->ai_addrlen) != 0) {
        fprintf(stderr, "connect failed\n");
        return 1;
    }
    freeaddrinfo(ai);

    if(libssh2_init(0) != 0) {
        fprintf(stderr, "libssh2_init failed\n");
        return 1;
    }
    LIBSSH2_SESSION *session = libssh2_session_init();
    if(!session) return 1;
    libssh2_session_set_blocking(session, 1);
    if(libssh2_session_handshake(session, sock)) {
        fprintf(stderr, "handshake failed\n");
        return 1;
    }
    printf("%s", libssh2_session_banner_get(session) ? "" : "");

    if(keyfile)
        rc = libssh2_userauth_publickey_fromfile(session, user, NULL, keyfile, "");
    else
        rc = libssh2_userauth_password(session, user, pass);
    if(rc) {
        fprintf(stderr, "authentication failed\n");
        goto shutdown;
    }

    LIBSSH2_CHANNEL *channel = libssh2_channel_open_session(session);
    if(!channel) {
        fprintf(stderr, "channel open failed\n");
        goto shutdown;
    }
    if(libssh2_channel_exec(channel, cmd)) {
        fprintf(stderr, "exec failed\n");
        goto shutdown;
    }
    for(;;) {
        char buf[4096];
        ssize_t n = libssh2_channel_read(channel, buf, sizeof(buf));
        if(n > 0) fwrite(buf, 1, (size_t)n, stdout);
        else break;
    }
    libssh2_channel_close(channel);
    libssh2_channel_free(channel);
    exitcode = 0;

shutdown:
    libssh2_session_disconnect(session, "done");
    libssh2_session_free(session);
    close(sock);
    libssh2_exit();
    return exitcode;
}
