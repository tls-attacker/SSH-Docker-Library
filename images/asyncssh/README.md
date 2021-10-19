## About
Small SSH server using [AsyncSHH](https://pypi.org/project/asyncssh/) that supports RSA key exchange. The server has a 2048 bit RSA host-key.

It is based on the Python 3 Docker image (see: https://hub.docker.com/_/python).


### Building and running

Build using:
```bash
docker build -t asyncssh-server .
```

Run using:
```bash
docker run -it -p 8022:22 --rm --name asyncssh asyncssh-server
```

The server is then available on localhost port 8022

### Authentication

Client authentication uses public keys or passwords. The authorized_keys folder contains one test key for the user with username *sshattacker*.
The test_key folder contains the private key for user *sshattacker* for authentication.

If you want to use your own keypair, add your public key to the authorized_keys folder, using your desired username as the filename.
Note: Do not add a file ending (e.g.: .pub), as the simple server will treat it as part of the username.

Using build arguments, a user with password can be created. The default values for this user can be found in the Dockerfile. 
Defining your own username and password could look like this:

```bash
docker build --build-arg DEFAULT_USERNAME=<username> --build-arg DEFAULT_PASSWORD=<password> -t asyncssh-server .
```