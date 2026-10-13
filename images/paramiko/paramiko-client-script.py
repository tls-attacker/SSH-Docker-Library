#!/usr/bin/env python
import os
import sys
import time

import paramiko
import click


# How to treat a host key that is not in ~/.ssh/known_hosts, from SSLC_HOSTKEY_PROMPT: accept (and store it there),
# accept_once (for this connection only) or reject
class HostKeyRejected(paramiko.SSHException):
    """The host key is unknown and SSLC_HOSTKEY_PROMPT says to reject it."""


class RejectUnknownHostKey(paramiko.MissingHostKeyPolicy):
    def missing_host_key(self, client, hostname, key):
        raise HostKeyRejected("Host key of %s is not in known_hosts" % hostname)


HOSTKEY_POLICIES = {
    "accept": paramiko.AutoAddPolicy,
    "accept_once": paramiko.WarningPolicy,
    "reject": RejectUnknownHostKey,
}
KNOWN_HOSTS = os.path.expanduser("~/.ssh/known_hosts")


def configure_host_keys(client):
    prompt = os.environ.get("SSLC_HOSTKEY_PROMPT", "accept")
    if prompt not in HOSTKEY_POLICIES:
        sys.exit("SSLC_HOSTKEY_PROMPT=%s is not supported by paramiko" % prompt)
    if prompt == "accept" and not os.path.exists(KNOWN_HOSTS):
        # AutoAddPolicy only stores accepted keys in a file loaded with load_host_keys
        os.makedirs(os.path.dirname(KNOWN_HOSTS), exist_ok=True)
        open(KNOWN_HOSTS, "a").close()
    if os.path.exists(KNOWN_HOSTS):
        client.load_host_keys(KNOWN_HOSTS)
    client.set_missing_host_key_policy(HOSTKEY_POLICIES[prompt]())


@click.command()
@click.option("-H", "--host", help="hostname or ip", default="127.0.0.1")
@click.option("-P", "--port", help="port", default=22, type=int)
@click.option("-u", "--username", help="username", default="sshattacker")
@click.option("-p", "--password", help="password", default="secret")
@click.option("-c", "--command", help="command", default="pwd")
@click.option(
    "-o",
    "--output",
    is_flag=True,
    show_default=True,
    default=False,
    help="print output",
)
@click.option(
    "-e", "--error", is_flag=True, show_default=True, default=False, help="print error"
)
def client_start(host, port, username, password, command, output, error):
    if output:
        print("init Client")
    client = paramiko.SSHClient()
    configure_host_keys(client)

    if output:
        print("start connection")
    print(
        "Trying to connect to: [host="
        + host
        + ", port="
        + str(port)
        + ", username="
        + username
        + ", password="
        + password
        + "]"
    )
    client.connect(host, port=port, username=username, password=password)

    if output:
        print("start command")
    stdin, stdout, stderr = client.exec_command(command)

    if output:
        print("finish")
    out = stdout.readlines()
    err = stderr.readlines()
    if output:
        print("output: ", "\n".join(out))
    if error:
        print("error: ", "\n".join(err))

    stdin.close()
    stdout.close()
    stderr.close()
    # Close the channel and connection explicitly. Otherwise CHANNEL_CLOSE is only sent by Channel.__del__ during
    # interpreter shutdown, racing the transport thread's death, so it is sometimes sent and sometimes not.
    stdout.channel.close()
    # If a message of the peer closed the channel (e.g. CHANNEL_CLOSE or CHANNEL_FAILURE), paramiko's transport thread
    # sends EOF and CLOSE itself, but only after marking the channel closed, which already woke us up. Give it time to
    # send them before closing the socket underneath it; short enough to stay within the learner's retrieve delay.
    time.sleep(0.05)
    client.close()


if __name__ == "__main__":
    client_start()
