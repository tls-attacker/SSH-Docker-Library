#!/usr/bin/env python3
"""
Server code from example of AsyncSSH, see:
https://asyncssh.readthedocs.io/en/stable/#server-examples
"""
import argparse
import pathlib
import asyncio
import asyncssh
import sys

PASSWORDS = {}
AUTHORIZED_KEYS_FILES = {}


async def handle_client(process):
    process.stdout.write("Enter numbers one per line, or EOF when done:\n")

    total = 0

    try:
        async for line in process.stdin:
            line = line.rstrip("\n")
            if line:
                try:
                    total += int(line)
                except ValueError:
                    process.stderr.write("Invalid number: %s\n" % line)
    except asyncssh.BreakReceived:
        pass

    process.stdout.write("Total = %s\n" % total)
    process.exit(0)


class MySSHServer(asyncssh.SSHServer):
    def connection_made(self, conn):
        self._conn = conn
        print("Connection established!")

    def begin_auth(self, username):
        try:
            authorized_keys_file = AUTHORIZED_KEYS_FILES[username]
        except KeyError:
            pass
        else:
            print(f"authorized_keys file for user {username}: {authorized_keys_file}")
            try:
                self._conn.set_authorized_keys(str(authorized_keys_file))
            except IOError as e:
                print(
                    "IO error occurred during begin_auth, maybe there is no key for this user."
                )
                print(e)
                print("Falling back to password authentication for user '%s'." % username)
                pass
        return True

    def password_auth_supported(self):
        # If there are no passwords, let's password-authentication will be
        # disabled.
        return bool(PASSWORDS)

    def validate_password(self, username, password):
        print("Validating password for user %s." % username)
        try:
            expected_password = PASSWORDS[username]
        except KeyError:
            return False
        else:
            return password == expected_password


async def start_server(port, host_keys):
    print(f"Server starting up on {port}...")
    await asyncssh.create_server(
        MySSHServer,
        "",
        port,
        server_host_keys=host_keys,
        process_factory=handle_client,
    )


def validate_username(text):
    stripped = text.strip()
    if not stripped:
        raise ValueError("Username must not be empty!")
    return stripped


def main(argv=None):
    parser = argparse.ArgumentParser()
    parser.add_argument("username", help="SSH username", type=validate_username)
    parser.add_argument(
        "-P",
        "--password",
        action="store",
        help="SSH password (password auth will be disabled if not specified)",
    )
    parser.add_argument(
        "-f",
        "--authorized-keys-file",
        type=pathlib.Path,
        action="store",
        help="SSH authorized_keys file (pubkey auth won't not work if not specified)",
    )
    parser.add_argument(
        "-p",
        "--port",
        action="store",
        type=int,
        default=22,
        help="SSH port to listen on",
    )
    parser.add_argument(
        "--host-key",
        action="store",
        type=pathlib.Path,
        default=pathlib.Path(__file__).parent.joinpath("ssh_host_key"),
        help="SSH host key",
    )
    args = parser.parse_args(argv)

    print(f"Username: {args.username!r}")
    if args.password is not None:
        print(f"Password: {args.password!r}")
        PASSWORDS[args.username] = args.password
    else:
        print("No password given, password auth will be disabled.")

    if args.authorized_keys_file is not None:
        print(f"authorized_keys file: {args.authorized_keys_file}")
        AUTHORIZED_KEYS_FILES[args.username] = args.authorized_keys_file
    else:
        print("No authorized_keys file given, pubkey auth will not work.")

    if args.password is None and args.authorized_keys_file is None:
        print("Neither password nor authorized_keys file specified, you won't be able to log in!")

    loop = asyncio.get_event_loop()
    try:
        loop.run_until_complete(start_server(port=args.port, host_keys=[args.host_key]))
    except (OSError, asyncssh.Error) as exc:
        sys.exit("Error starting server: " + str(exc))
    loop.run_forever()
    return 0


if __name__ == "__main__":
    sys.exit(main())
