#include <libssh/libssh.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>

int verify_knownhost(ssh_session session)
{
  int state, hlen;
  unsigned char *hash = NULL;
  char *hexa;
  char buf[10];

  state = ssh_is_server_known(session);

  hlen = ssh_get_pubkey_hash(session, &hash);
  if (hlen < 0)
    return -1;

  switch (state)
  {
    case SSH_SERVER_KNOWN_OK:
      break;

    case SSH_SERVER_KNOWN_CHANGED:
      fprintf(stderr, "Host key for server changed: it is now:\n");
      ssh_print_hexa("Public key hash", hash, hlen);
      break;

    case SSH_SERVER_FOUND_OTHER:
      fprintf(stderr, "The host key for this server was not found but an other"
        "type of key exists.\n");
      break;

    case SSH_SERVER_FILE_NOT_FOUND:
      fprintf(stderr, "Could not find known host file.\n");

    case SSH_SERVER_NOT_KNOWN:
      hexa = ssh_get_hexa(hash, hlen);
      fprintf(stderr, "Public key hash: %s\n", hexa);
      free(hexa);
      break;

    case SSH_SERVER_ERROR:
      fprintf(stderr, "Error %s", ssh_get_error(session));
      free(hash);
      return -1;
  }

  free(hash);
  return 0;
}
 
int show_remote_processes(ssh_session session, char *command)
{
  ssh_channel channel;
  int rc;
  char buffer[256];
  int nbytes;
 
  channel = ssh_channel_new(session);
  if (channel == NULL)
    return SSH_ERROR;
 
  rc = ssh_channel_open_session(channel);
  if (rc != SSH_OK)
  {
    ssh_channel_free(channel);
    return rc;
  }
 
  rc = ssh_channel_request_exec(channel, command);
  if (rc != SSH_OK)
  {
    ssh_channel_close(channel);
    ssh_channel_free(channel);
    return rc;
  }
 
  nbytes = ssh_channel_read(channel, buffer, sizeof(buffer), 0);
  while (nbytes > 0)
  {
    if (write(1, buffer, nbytes) != (unsigned int) nbytes)
    {
      ssh_channel_close(channel);
      ssh_channel_free(channel);
      return SSH_ERROR;
    }
    nbytes = ssh_channel_read(channel, buffer, sizeof(buffer), 0);
  }
 
  if (nbytes < 0)
  {
    ssh_channel_close(channel);
    ssh_channel_free(channel);
    return SSH_ERROR;
  }
 
  ssh_channel_send_eof(channel);
  ssh_channel_close(channel);
  ssh_channel_free(channel);
 
  return SSH_OK;
}

int main(int argc, char **argv)
{
  ssh_session ssh_session;
  int rc;

  char *password;
  int port = 22;
  char *host;
  char *user;
  char *command;

  if(argc != 6){
    fprintf(stderr, "wrong arg count");
    exit(-1);
  }


  host = argv[1];
  port = atoi(argv[2]);
  user = argv[3];
  password = argv[4];
  command = argv[5]; 

  fprintf(stderr, "PARAMETER OK\n");
  // Open session and set options
  ssh_session = ssh_new();
  if (ssh_session == NULL){
    exit(-1);
  }

  fprintf(stderr, "SSH SESSION CREATED\n");

  fprintf(stderr, "host: %s\n", host);

  if(ssh_options_set(ssh_session, SSH_OPTIONS_HOST, host) < 0){
    fprintf(stderr, "OPTION HOST ERROR\n");
  }

  if(ssh_options_set(ssh_session, SSH_OPTIONS_PORT, &port) < 0){
    fprintf(stderr, "OPTION PORT ERROR\n");
  }

  if(ssh_options_set(ssh_session, SSH_OPTIONS_USER, user) < 0){
    fprintf(stderr, "OPTION USER ERROR\n");
  }

  fprintf(stderr, "SSH OPTIONS OK\n");
  // Connect to server
  rc = ssh_connect(ssh_session);
  fprintf(stderr, "SSH CONNECTION START OK\n");
  if (rc != SSH_OK)
  {
    fprintf(stderr, "Error connecting to host: %s\n",
            ssh_get_error(ssh_session));
    ssh_free(ssh_session);
    exit(-1);
  }
  fprintf(stderr, "SSH CONNECT OK\n");

 
  // Verify the server's identity
  if (verify_knownhost(ssh_session) < 0)
  {
    fprintf(stderr, "Error verify");
    ssh_disconnect(ssh_session);
    ssh_free(ssh_session);
    exit(-1);
  }

  fprintf(stderr, "SSH VERIFY HOST OK\n");
  // Authenticate ourselves
  rc = ssh_userauth_password(ssh_session, NULL, password);
  if (rc != SSH_AUTH_SUCCESS)
  {
    fprintf(stderr, "Error authenticating with password: %s\n",
            ssh_get_error(ssh_session));
    ssh_disconnect(ssh_session);
    ssh_free(ssh_session);
    exit(-1);
  }

  fprintf(stderr, "SSH AUTH OK\n");
 
  rc = show_remote_processes(ssh_session,command);
  if(rc != SSH_OK){
    fprintf(stderr, "Error on exec command\n");
    ssh_disconnect(ssh_session);
    ssh_free(ssh_session);
    exit(-1);
  }

  fprintf(stderr, "SSH COMMAND EXEC OK\n");

  ssh_disconnect(ssh_session);
  ssh_free(ssh_session);
  exit(0);
}