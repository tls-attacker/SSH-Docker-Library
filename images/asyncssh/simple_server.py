import asyncio
import asyncssh
import sys


# Server code from example of AsyncSSH, see: https://asyncssh.readthedocs.io/en/stable/#server-examples
async def handle_client(process):
    process.stdout.write('Enter numbers one per line, or EOF when done:\n')

    total = 0

    try:
        async for line in process.stdin:
            line = line.rstrip('\n')
            if line:
                try:
                    total += int(line)
                except ValueError:
                    process.stderr.write('Invalid number: %s\n' % line)
    except asyncssh.BreakReceived:
        pass

    process.stdout.write('Total = %s\n' % total)
    process.exit(0)


class MySSHServer(asyncssh.SSHServer):
    def connection_made(self, conn):
        self._conn = conn
        print("Connection established!")

    def begin_auth(self, username):
        try:
            self._conn.set_authorized_keys('authorized_keys/%s' % username)
            print("Authorized keys set.")
        except IOError as e:
            print("IO error occurred during begin_auth, maybe there is no key for this user.")
            print(e)
            print("Falling back to password authentication for user '%s'." % username)
            pass
        return True

    def password_auth_supported(self):
        return True

    def validate_password(self, username, password):
        print("Validating password for user %s." % username)
        pw = passwords.get(username, '*')
        return password == pw


async def start_server():
    print("Server starting up...")
    create_default_user(sys.argv)
    await asyncssh.create_server(MySSHServer, '', 22,
                                 server_host_keys=['ssh_host_key'],
                                 process_factory=handle_client)


def create_default_user(arguments):
    if len(arguments) != 3:
        print("Number of arguments is: %d. For creation of a default user, we need a username and password." % (
                    len(arguments) - 1))
    else:
        print("Default username: %s" % arguments[1])
        print("Default password: %s" % arguments[2])
        passwords.update({arguments[1]: arguments[2]})


passwords = {'test': 'test'}


loop = asyncio.get_event_loop()

try:
    loop.run_until_complete(start_server())
except (OSError, asyncssh.Error) as exc:
    sys.exit('Error starting server: ' + str(exc))

loop.run_forever()
