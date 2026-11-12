# SSH Docker Library

## Quickstart

First, you need to decide which SSH server you want to start.
You can get a list of supported services using the following command:

    $ docker compose config --services
    openssh-7.0p1
    openssh-7.1p1
    openssh-7.1p2
    openssh-7.2p1
    openssh-7.2p2
    openssh-7.3p1
    [...]

Now, you can start start a service:

    $ docker compose up openssh-7.2p1 --build --detach
    [+] Running 1/1
    ⠿ Container ssh-docker-library-openssh-7.2p1-1  Started

To connect to it from the docker host, you need to know which port on the host machine is mapped to the SSH port (22) of the container:

    $ docker compose port openssh-7.2p1 22
    0.0.0.0:22203

After determining the correct port, you can connect to the SSH server using the `ssh` command from the docker host:

    $ ssh -p 22203 sshattacker@172.17.0.1
    The authenticity of host '[172.17.0.1]:22203 ([172.17.0.1]:22203)' can't be established.
    ED25519 key fingerprint is SHA256:G3hVr1Zn4TpUoyd5zqqdn55zgpLUa2UV7V3w1+9KRUQ.
    This key is not known by any other names
    Are you sure you want to continue connecting (yes/no/[fingerprint])?

## Covered implementations

Generated from the per-image `specs.json` files; regenerate together with
`compose.yml` via `tools/generate_compose_config.py`.

| Directory | Roles | Server versions | Client versions | Newest | Upstream |
|---|---|---|---|---|---|
| `apache-sshd` | server | 9 | 0 | `2.18.0` | https://mina.apache.org/sshd-project/ |
| `asyncssh` | server/client | 58 | 39 | `2.24.1` | https://asyncssh.readthedocs.io/en/latest/ |
| `bitvise` | server/client | 54 | 54 | `9.43` | https://www.bitvise.com/index |
| `dropbear` | server/client | 69 | 69 | `2026.94` | https://matt.ucc.asn.au/dropbear/CHANGES |
| `erlang-ssh` | server/client | 152 | 152 | `29.1.2` | https://www.erlang.org/doc/man/ssh.html |
| `go-crypto-ssh` | server/client | 10 | 10 | `0.58.0` | https://pkg.go.dev/golang.org/x/crypto/ssh |
| `libssh` | server/client | 24 | 24 | `0.12.2` | https://www.libssh.org/files/ |
| `libssh-0.7.x-0.8.x` | server | 18 | 0 | `0.8.9` | https://www.libssh.org/files/ |
| `libssh2` | client | 0 | 10 | `1.11.1` | https://libssh2.org/ |
| `libssh_mbedTLS` | server | 15 | 0 | `0.12.2` | https://git.libssh.org/projects/libssh.git/ |
| `node-ssh2` | server/client | 10 | 10 | `1.17.0` | https://github.com/mscdex/ssh2 |
| `openssh` | server/client | 29 | 29 | `10.6p1` | https://www.openssh.com/releasenotes.html |
| `openssh-7.x` | server/client | 10 | 10 | `7.9p1` | https://www.openssh.com/releasenotes.html |
| `paramiko` | client | 0 | 37 | `5.1.0` | https://docs.paramiko.org/en/stable/api/client.html |
| `phpseclib` | client | 0 | 10 | `4.0.3` | https://github.com/phpseclib/phpseclib |
| `pkix-ssh` | server/client | 16 | 16 | `18.0.2` | https://roumenpetrov.info/secsh/ |
| `putty` | client | 0 | 15 | `0.85` | https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html |
| `russh` | server/client | 70 | 70 | `0.65.1` | https://github.com/warp-tech/russh |
| `ssh-net` | client | 0 | 10 | `2026.0.0` | https://github.com/sshnet/SSH.NET |
| `sshd-lite` | server | 80 | 0 | `1.55.4` | https://github.com/jpillora/sshd-lite |
| `tinyssh` | server | 38 | 0 | `20261101` | https://github.com/janmojzis/tinyssh |
| `twisted-conch` | server/client | 10 | 10 | `26.10.0` | https://github.com/twisted/twisted |
| `wolfssh` | server/client | 16 | 16 | `1.6.0` | https://www.wolfssl.com/docs/wolfssh-changelog/ |

Not covered, and why:

* **Tera Term/TTSSH, WinSCP, the Bitvise client and the mobile clients** are
  Windows- or Android-only and cannot be built in a Linux container, even
  though OpenSSH's compatibility table carries rows for several of them.
* **HPN-SSH** ships its releases as patch sets against OpenSSH; the portable
  repository carries only development branches and markers, with no release
  tag or tarball scheme a version could be pinned to.
* **lsh** last released in 2013 and needs `liboop` and a nettle 2.x that are
  no longer packaged on any current base image.

## Building

You can easily build the different Docker images using [docker compose](https://docs.docker.com/compose/):

    $ docker compose up openssh-7.2p1

To modify the build, you can edit the [`docker-compose.yml` configuration file](https://docs.docker.com/compose/compose-file/) (or rather [add a `docker-compose.override.yml`](https://docs.docker.com/compose/extends/#multiple-compose-files) file).
For more details, please refer to the official Docker documentation.

Alternatively, you can also use `docker build` directly:

    $ docker build --tag openssh:7.2p1 --build-arg VERSION=7.2p1 images/openssh-7.x/
    Sending build context to Docker daemon  11.78kB
    Step 1/27 : FROM alpine:latest AS openssh-downloader
     ---> 9c6f07244728
    [...]

### Build Arguments

The docker images can be customized by [variables that can be overridden at build-time](https://docs.docker.com/engine/reference/commandline/build/#set-build-time-variables---build-arg):

| Build-Time Variable | Default Value   | Description                                                                                                                            |
| ------------------- | --------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `USERNAME`          | `sshattacker`   | SSH username                                                                                                                           |
| `PASSWORD`          | `secret`        | SSH password (if password authentication is supported by the server)                                                                   |
| `DOTSSH`            | `dotssh-server` | Path to directory in Docker build context that is used as user configuration directory. This is `dotssh-client` for client containers. |
| `WITH_NONE_CIPHER`  | `1`             | Whether to apply patch to enable the `none` cipher, set to `0` to disable _(OpenSSH only)_                                             |

## Example

Here is how to build everything and then to connect to each server with each key.

:warning: NOTE: There is a bug that prevents all images from being built at the same time (see <https://github.com/docker/compose/issues/9837>). It can be helpful to build in batches, for example with `docker compose config --services | xargs --max-args=5 --max-procs=1 docker compose build`.

    $ docker compose up --build
    $ for service in $(docker compose config --services); do port="$(docker compose port "$service" 22 | cut -f2 -d:)"; for key in id_rsa id_ecdsa id_ed25519; do ssh -i "$key" -p "$port" -o StrictHostKeyChecking=no -o PasswordAuthentication=no sshattacker@172.17.0.1 echo "$service: login succeeded using $key" || echo "$service: login failed using $key"; done; done

Note that the client containers are put into the profile `client` and not started automatically. They can be used to access SSH servers in several ways.

    $ docker compose --profile client build
    $ docker run -u sshattacker --add-host=host.docker.internal:host-gateway -ti --rm rub-nds/openssh-client:9.0p1 host.docker.internal -p 22320
    $ docker run -u sshattacker --network=host -ti --rm rub-nds/openssh-client:9.0p1 localhost -p 22320
    $ docker run -u sshattacker --network=ssh-docker-library_default -ti --rm rub-nds/openssh-client:9.0p1 ssh-docker-library-openssh-server-9.0p1-1

| SSH-Client implementation | Commandline parameter specification                                                                                                |
| ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| asyncssh                  | [--host host] [--port portnummer] [--username username] [--password password] [--command command] -o(print output) -e(print error) |
| bitvise                   | [login@]hostname -pw=password -port=portnum                                                                                        |
| dropbear                  | [-Tt] [-p port] [-i id] [-L l:h:r] [-R l:h:r] [-l user] host [command]                                                             |
| erlang-ssh                | ip port user password command                                                                                                      |
| libssh                    | [login@]hostname [-l user] [-p port] [-T proxycommand]                                                                             |
| openssh                   | [login@]hostname [-l login_name] [-p port] [-i identity_file]                                                                      |
| paramiko                  | [--host host] [--port portnum] [--username username] [--password password] [--command command] -o(print output) -e(print error)    |
| puttylinux                | [login@]hostname [-P port] [-pw password]                                                                                          |
| russh                     | host:port --username username --password password                                                                                  |
| wolfssh                   | [-h host] [-p port] [-u username] [-p &lt;password&gt;] [-i filename filename for the user's private key]                          |

Some of the client implementations have an automated variant that is non-interactive and can be used in scripts.
Automated clients will attempt to login with the provided credentials and execute a command on the server.

## Label

Each image is labeled with the following metadata:

| Label Key                    | Example   | Description                                         |
| ---------------------------- | --------- | --------------------------------------------------- |
| `ssh.implementation.name`    | `openssh` | Name of the SSH implementation                      |
| `ssh.implementation.version` | `9.0p1`   | Upstream version of the SSH implementation          |
| `ssh.implementation.type`    | `server`  | `server` for SSH server and `client` or `client-automated` for SSH client |

You can filter the docker images with `docker images -f label=ssh.implementation.name` or `docker images -f label=ssh.implementation.type=server`, for example.
