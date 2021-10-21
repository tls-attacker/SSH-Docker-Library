## LibSSH

[LibSSH](https://www.libssh.org/) is an SSH library written in C which covers a wide range of features of the SSH ecosystem. The entire feature set can be found [here](https://www.libssh.org/features/). In order to get a server up and running this image uses the included `ssh_server_fork` example.

The server was configured to offer an DSA, ECDSA (nistp256) and RSA (4096 bit) host key. Additionally, if running version 0.9.0 or later, the server also offers an ed25519 host key. All included host keys can be found in the `host_keys` folder.

### Prerequisites

The following base images must be present in order to build the libssh image:

- `debian-build:bullseye` (if version >= 0.9.0)
- `debian-build:stretch-libssl1.0` (if 0.7.0 <= version < 0.9)

### Building and Running

#### Server

Build the latest supported server version using:

`docker build -f Dockerfile-0_9_x --target libssh-server -t libssh-server .`

You may specify the username, password and libssh version by using the `USERNAME`, `PASSWORD` and `VERSION` build args. If building version < 0.9.0 you have to adjust the Dockerfile accordingly. To build all supported libssh versions simply use the `./build.sh` script.

Run the libssh server using:

`docker --rm libssh-server`

### Supported Versions

- 0.9.0, 0.9.1, 0.9.2, 0.9.3, 0.9.4, 0.9.5, 0.9.6
- 0.8.0, 0.8.1, 0.8.2, 0.8.3, 0.8.4, 0.8.5, 0.8.6, 0.8.7, 0.8.8, 0.8.9
- 0.7.0, 0.7.1, 0.7.2, 0.7.3, 0.7.4, 0.7.5, 0.7.6, 0.7.7
