#!/usr/bin/env python
# pyright: reportGeneralTypeIssues=false

import click
import asyncio
import asyncssh
import os
import sys


class HostKeyPolicyClient(asyncssh.SSHClient):
    """Decides on host keys that are not in ~/.ssh/known_hosts as ACCEPT_HOSTKEY says."""

    def __init__(self, accept):
        self._accept = accept

    def validate_host_public_key(self, host, addr, port, key):
        return self._accept


def host_key_options():
    """
    The connect options for ACCEPT_HOSTKEY: once (accept) or no (reject) for a key that is not in ~/.ssh/known_hosts.
    asyncssh never writes known_hosts, so it cannot accept and store a key. Without ACCEPT_HOSTKEY, host keys
    are not checked at all, as before.
    """
    prompt = os.environ.get("ACCEPT_HOSTKEY")
    if prompt is None:
        return {"known_hosts": None}
    if prompt not in ("once", "no"):
        sys.exit("ACCEPT_HOSTKEY=%s is not supported by asyncssh" % prompt)
    accept = prompt == "once"
    # known_hosts left at its default: ~/.ssh/known_hosts, if it exists
    return {"client_factory": lambda: HostKeyPolicyClient(accept)}


@click.command()
@click.option("-H", "--host", help="hostname or ip", default='172.17.0.1')
@click.option("-p", "--port", help="prot", default=22, type=int)
@click.option("-u", "--username", help="username", default="sshattacker")
@click.option("-P", "--password", help="password", default="secret")
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
    print(f"Connecting to {host}:{port} as {username}")

    try:
        asyncio.run(
            run_client(host, port, username, password, command, output, error)
        )
    except (OSError, asyncssh.Error) as exc:
        sys.exit("SSH connection failed: " + str(exc))


async def run_client(host, port, username, password, command, output, error):
    async with asyncssh.connect(
        host=host, port=port, username=username, password=password, **host_key_options()
    ) as conn:
        try:
            result = await conn.run(command, check=True)
        except asyncssh.ProcessError as exc:
            if error:
                print(exc.stderr, end='')
                print(f'Process exited with status {exc.exit_status}',
                      file=sys.stderr)
        else:
            if output:
                print(result.stdout, end='')

if __name__ == "__main__":
    client_start()
