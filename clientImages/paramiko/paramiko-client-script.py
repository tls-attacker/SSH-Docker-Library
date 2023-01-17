#!/usr/bin/env python

import paramiko
import click

@click.command()
@click.option('-H', '--host', help='hostname or ip', default='172.17.0.1"')
@click.option('-P', '--port', help='prot', default=3022, type=int)
@click.option('-u', '--username', help='username', default='demo')
@click.option('-p', '--password', help='password', default='password')
@click.option('-c', '--command', help='command', default='pwd')
@click.option('-o', '--output',  is_flag=True, show_default=True, default=False, help='print output')
@click.option('-e', '--error',  is_flag=True, show_default=True, default=False, help='print error')
def client_start(host, port, username, password, command, output, error):
    print(f'host {host}\n')
    print(f'port {port}\n')
    print(f'username {username}\n')
    if output:
        print("init Client")
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    if output:
        print("start connection")
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

if __name__ == '__main__':
    client_start()


