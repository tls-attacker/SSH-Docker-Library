#!/usr/bin/env python
import os
import sys
import time

import paramiko
import click


# How to treat a host key that is not in ~/.ssh/known_hosts, from ACCEPT_HOSTKEY: yes (accept and store it there),
# once (accept for this connection only) or no (reject)
class HostKeyRejected(paramiko.SSHException):
    """The host key is unknown and ACCEPT_HOSTKEY says to reject it."""


class RejectUnknownHostKey(paramiko.MissingHostKeyPolicy):
    def missing_host_key(self, client, hostname, key):
        raise HostKeyRejected("Host key of %s is not in known_hosts" % hostname)


HOSTKEY_POLICIES = {
    "yes": paramiko.AutoAddPolicy,
    "once": paramiko.WarningPolicy,
    "no": RejectUnknownHostKey,
}
KNOWN_HOSTS = os.path.expanduser("~/.ssh/known_hosts")


def configure_host_keys(client):
    prompt = os.environ.get("ACCEPT_HOSTKEY", "yes")
    if prompt not in HOSTKEY_POLICIES:
        sys.exit("ACCEPT_HOSTKEY=%s is not supported by paramiko" % prompt)
    if prompt == "yes" and not os.path.exists(KNOWN_HOSTS):
        # AutoAddPolicy only stores accepted keys in a file loaded with load_host_keys
        os.makedirs(os.path.dirname(KNOWN_HOSTS), exist_ok=True)
        open(KNOWN_HOSTS, "a").close()
    if os.path.exists(KNOWN_HOSTS):
        client.load_host_keys(KNOWN_HOSTS)
    client.set_missing_host_key_policy(HOSTKEY_POLICIES[prompt]())


def load_pubkeys(key_file):
    if key_file == "id_rsa":
        key = paramiko.RSAKey.from_private_key_file(".ssh/id_rsa")
    elif key_file == "id_dsa":
        # paramiko 4.0 removed DSA (DSSKey); without it this raises and the client falls back to password auth
        if not hasattr(paramiko, "DSSKey"):
            raise paramiko.SSHException("DSA keys are not supported by this paramiko version")
        key = paramiko.DSSKey.from_private_key_file(".ssh/id_dsa")
    elif key_file == "id_ecdsa":
        key = paramiko.ECDSAKey.from_private_key_file(".ssh/id_ecdsa")
    else:
        key = paramiko.Ed25519Key.from_private_key_file(".ssh/id_ed25519")
    return key


@click.command()
@click.option("-H", "--host", help="hostname or ip", default="127.0.0.1")
@click.option("-P", "--port", help="port", default=22, type=int)
@click.option("-u", "--username", help="username", default="sshattacker")
@click.option("-p", "--password", help="password", default="secret")
@click.option("-c", "--command", help="command", default="pwd")
@click.option("-i", "--key_file", show_default=True, default="id_ecdsa")
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
def client_start(host, port, username, password, command, key_file, output, error):
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
    try:
        key = load_pubkeys(key_file)
        client.connect(host, port=port, username=username, password=password, pkey=key)
    except (HostKeyRejected, paramiko.BadHostKeyException):
        # retrying without the key would only open a second connection to the server
        raise
    except Exception as e:
        print("Loading pubkeys failed, may need to use a newer version of paramiko")
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
