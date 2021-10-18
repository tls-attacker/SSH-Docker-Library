## About
Small SSH server using [AsyncSHH](https://pypi.org/project/asyncssh/) that supports RSA key-exchange. The server has a 2048 bit RSA key.


### Building and running

Build using:
```bash
docker build -t asyncssh-server .
```

Run using:
```bash
docker run -it -p 8022:8022 --rm --name asyncssh asyncssh-server
```

The server is then available on localhost port 8022

### Authentication

Client authentication uses public keys. The authorized_keys folder contains one test key for the user with username *test*.
The test_key folder contains the private key for user *test* for authentication.