#!/usr/bin/env python
import paramiko
import click


def load_pubkeys(key_file):
    if key_file == "id_rsa":
        key = paramiko.RSAKey.from_private_key_file(".ssh/id_rsa")
    elif key_file == "id_dsa":
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
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

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


if __name__ == "__main__":
    client_start()
