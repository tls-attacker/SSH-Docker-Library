# About

Small SSH server using [AsyncSHH](https://pypi.org/project/asyncssh/) that supports RSA key exchange. The server has a 2048 bit RSA host-key.

It is based on the Python 3.9-slim-bullseye Docker image (see: <https://hub.docker.com/_/python>).

Additionally, this project can be used to create AsyncSSH servers with certain vulnerabilities.
All of these should **never** be used in production.
They were created for testing purposes for the Master's thesis:
_"Evaluating the Impact of Manger's attack on the SSH ecosystem"_

## Building and running

Build using:

```bash
docker build -t asyncssh281-server .
```

This project contains different Dockerfiles to create different servers.
You can use the '-f' flag to select one of these for your build.
For example:

```bash
docker build -t asyncssh-server -f Dockerfile .
```

Run using:

```bash
docker run -p 8022:22 --rm --name asyncssh281-server
```

The server is then reachable on localhost:8022.

## Authentication

Client authentication uses public keys or passwords. The `authorized_keys`
directory contains one test public key for the user with username `sshattacker`.
The `login_keys` directory contains the private key for user `sshattacker\* for authentication.

If you want to add your own key pair, add your public key to the authorized_keys folder,
using `<desired_username>.pub` as the filename.

Using build arguments, a user with password can be created. The default values for this user can be found in the Dockerfile.
Defining your own username and password could look like this:

```bash
docker build --build-arg DEFAULT_USERNAME=<username> --build-arg DEFAULT_PASSWORD=<password> -t asyncssh281-server .
```

## Dockerfiles

This project contains 6 Dockerfiles that create different versions of an AsyncSSH v2.8.1 server.

1. Dockerfile: Creates a regular AsyncSSH v2.8.1 server
2. Dockerfile_oracle: Creates an AsyncSSH v2.8.1 server that provides Manger's oracle (Message content)
3. Dockerfile_oracle2: Creates an AsyncSSH v2.8.1 server that provides Manger's oracle (Message amount)
4. Dockerfile_oracle3: Creates an AsyncSSH v2.8.1 server that provides Manger's oracle (Message type / socket state)
5. Dockerfile_static_key: Creates an AsyncSSH v2.8.1 server that re-uses the transient public key in RSA key-exchanges.
6. Dockerfile_vuln: Creates an AsyncSSH v2.8.1 server that is vulnerable to Manger's attack.
   It re-uses the transient public key for RSA key-exchanges and provides Manger's oracle that reacts with
   a different error message if an invalid secret message received in the RSA key exchanges starts with a 00 byte or not.

All '\_oracle' Dockerfiles create a server with different types of Manger's oracle.
The vulnerable server has the same oracle as the server created by Dockerfile_oracle.

For more information on Manger's attack, see: <https://link.springer.com/content/pdf/10.1007/3-540-44647-8_14.pdf>
