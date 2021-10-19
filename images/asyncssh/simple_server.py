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
            print("IO error occurred during begin_auth.")
            print(e)
            pass
        return True


async def start_server():
    print("Server starting up...")
    await asyncssh.create_server(MySSHServer, '', 22,
                                 server_host_keys=['ssh_host_key'],
                                 process_factory=handle_client)


loop = asyncio.get_event_loop()

try:
    loop.run_until_complete(start_server())
except (OSError, asyncssh.Error) as exc:
    sys.exit('Error starting server: ' + str(exc))

loop.run_forever()
