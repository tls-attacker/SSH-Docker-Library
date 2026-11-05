# Extending the SSH-Docker-Library (2026-11-05)

Two changes: every family brought up to its current upstream release, and
eight new implementations added. Everything follows the library's own
conventions, so it regenerates with `tools/generate_compose_config.py` and is
upstream-able as-is.

## 1. Version bumps — 232 new entries

| family | was | now | added |
|---|---|---|---|
| openssh | 10.0p2 | 10.6p1 | 6 |
| dropbear | 2025.88 | 2026.94 | 6 |
| libssh | 0.11.2 | 0.12.2 | 6 |
| libssh_mbedTLS | 0.10.5 | 0.12.2 | 10 |
| asyncssh | 2.21.0 | 2.24.1 | 6 |
| paramiko | 2.11.0 | 5.1.0 | 16 |
| russh | 0.51.1 | 0.65.1 | 40 |
| wolfssh | 1.4.14 | 1.6.0 | 10 |
| tinyssh | 20250501 | 20261101 | 7 |
| sshd-lite | 1.6.0 | 1.55.4 | 68 |
| erlang-ssh | 27.3.0.0 | 29.1.2 | 51 |
| putty | 0.79 | 0.85 | 6 |

Versions come from the authoritative upstream source for each project (the
OpenBSD CDN, libssh.org, PyPI, crates.io, Docker Hub, the GitHub tag API),
not from a third-party comparison page.

## 2. New implementations — eight families

| directory | roles | language | note |
|---|---|---|---|
| `go-crypto-ssh` | server + client | Go | `golang.org/x/crypto/ssh`, the stack behind Docker, Gitea, Teleport, Tailscale |
| `apache-sshd` | server | Java | Apache MINA SSHD, used by Gerrit and Jenkins |
| `node-ssh2` | server + client | JavaScript | the `ssh2` npm module |
| `twisted-conch` | server + client | Python | OpenSSH's compat table carries two `Twisted*` rows |
| `pkix-ssh` | server + client | C | Roumen Petrov's OpenSSH fork with X.509 support |
| `libssh2` | client | C | client-only library behind curl, git and PHP |
| `phpseclib` | client | PHP | pure-PHP SSH2 |
| `ssh-net` | client | C# | SSH.NET (Renci.SshNet) |

Each ships the thin application around the library in `files/`, in the same
shape as the existing `asyncssh` image's `simple_server.py`.

## 3. Bugs fixed in the existing library

* **`putty/Dockerfile` could not be built at all.** Its `debian:bullseye` base
  still points at `deb.debian.org/debian-security`, which now 404s for every
  package. Added the same `sed` to `archive.debian.org` that the openssh and
  libssh images already carry. All six PuTTY versions build again.
* **`russh/Dockerfile` could not be built at all.** It fetched
  `github.com/tls-attacker/russh-server`, which returns HTTP 404 for the
  repository and for all three commits it pinned. Rewritten to build russh's
  own `echoserver` and `client_exec_simple` examples from the upstream tag.
* **`sshd-lite/Dockerfile` pinned `golang:1.19`**, which cannot build any
  release after 1.6 (they need `crypto/ecdh` and `slices`). Parameterised the
  Go version and set `GOTOOLCHAIN=auto`.
* **`tinyssh/specs.json` carried sshd-lite's URL.** Corrected.
* **`phpseclib`'s base image had no `unzip` and no zip extension**, so composer
  could not extract any dist package and every install failed. Added `unzip`
  and `git`. (This was a bug in the image added here, found by the smoke test.)
* **`wolfssh` pinned wolfSSL 5.6.3 for every version.** wolfSSH 1.4.15+ needs
  symbols that release does not export (`wc_ForceZero`), while wolfSSH
  <= 1.4.14 does not compile against wolfSSL 5.8.x at all (`'heap' undeclared`).
  The wolfSSL release is now chosen by the wolfSSH version, so both ends of the
  range build.
* **`libssh_mbedTLS` failed to link libssh 0.12's X11 example** (`pthread_create`
  undefined). Added `-DCMAKE_EXE_LINKER_FLAGS=-pthread`.
* **`templates/implementation.yml.tpl` emitted version strings unquoted**, so
  YAML coerced every version that looks like a number: `0.80` became `0.8`,
  `2026.90` became `2026.9`, `16.0` became `16`. The affected services were
  built with a `VERSION` that upstream never released and failed on download.
  This is a pre-existing bug; it only became visible here because the new
  version lists contain more such strings. Fixed by quoting at all four sites
  in the template. `docker build --build-arg VERSION=0.80` succeeded where
  `docker compose build` failed, which is what isolated it.
* **`russh/Dockerfile` hard-coded the client example name.** Releases up to
  0.39.0 ship it as `client`, later ones as `client_exec_simple`. The build now
  picks whichever the tag contains, which recovers 0.34.0 through 0.36.2.
* **`sshd-lite` 1.36.0 ships a `go.work` whose `go` directive is lower than
  its own module requirement**, so the toolchain refuses the workspace. The
  workspace adds nothing to this single-module build; `GOWORK=off` skips it.
* **composer refuses to install the phpseclib versions the corpus needs.**
  Every 3.0.53-3.0.56 and 4.0.0 release is covered by advisory
  `PKSA-12sz-bcny-pk4m`, and recent composer blocks affected versions by
  default. The corpus deliberately carries vulnerable releases - that is its
  purpose - so the image passes `--no-security-blocking`, with a comment
  saying why. **This is a security-relevant default being switched off on
  purpose; it is a one-line revert if that is not wanted.**

## 4. Deliberately not added

* **Tera Term/TTSSH, WinSCP, the Bitvise client, the mobile clients** —
  Windows- or Android-only, not containerisable on Linux. Worth noting that
  OpenSSH's compatibility table has rows for TTSSH and WinSCP, so the corpus
  still cannot exercise the implementations those rows describe.
* **HPN-SSH** — releases are patch sets against OpenSSH; the portable
  repository has only development branches and markers, with no release tag or
  tarball scheme to pin a version to.
* **lsh** — last released 2013, needs `liboop` and nettle 2.x, neither
  packaged on any current base image.

## 5. Versions removed from specs

These entries were in the library (or would have been added by the bump) and
cannot be built. Each was reproduced individually; the reason is the compiler
or package-manager error, not a transient download failure.

| Family | Versions | Reason |
|---|---|---|
| russh | 0.37.0, 0.37.1, 0.38.0, 0.39.0, 0.48.0, 0.54.6, 0.56.0, 0.57.0, 0.57.1, 0.58.0, 0.58.1, 0.59.0, 0.60.0, 0.60.1, 0.61.0, 0.61.1 | Upstream publishes no `Cargo.lock`, and these releases depend on RustCrypto **pre-releases** (`rsa = "0.10.0-rc.10"` and friends). Cargo now resolves those requirements to the finished releases, whose APIs differ, so the build fails inside `internal-russh-forked-ssh-key`, `crypto-bigint` or the error plumbing. Pinning `rsa` precisely only moves the failure to the next unpinned pre-release; reproducing these builds needs a lockfile that was never published. Buildability is not monotonic in the version - 0.60.2, 0.60.3, 0.61.2 and everything from 0.62.0 build - so no cutoff would do. |
| libssh2 | 1.5.0, 1.6.0, 1.7.0 | Embed `EVP_MD_CTX` by value, which OpenSSL made opaque in 1.1. Hard compile errors (`field 'exchange_hash' has incomplete type`), not deprecation warnings, so no compiler flag helps. Would require vendoring OpenSSL 1.0.2. |
| pkix-ssh | 12.5 | Downloads and configures, then fails in `make` against OpenSSL 3. |
| sshd-lite | 1.23.1 | References `ssh.Auth`, removed from `golang.org/x/crypto/ssh`, and the tag pins no x/crypto version that would still have it. |

## 6. Verification

`verify_library.py` builds each service, starts it, reads the identification
string off the wire and runs a full paramiko handshake, then deletes the image
so the VM disk does not fill. Results land in `/private/tmp/verify_results.jsonl`.

A server counts as working only if it starts **and** puts a valid SSH
identification string on the wire. Authentication is recorded separately,
because several library images ship without usable credentials (`erlang-ssh`
and `sshd-lite` start with no auth configured) or negotiate algorithms paramiko
does not implement (`tinyssh` offers chacha20-poly1305 only).

Final state, after the removals in section 5: **370 services checked, 370
built.** Of the 276 servers, 275 put a valid identification string on the
wire and 163 also accept the corpus key; the one exception is
`wolfssh-server-1.6.0`, which builds and starts but emits nothing before the
handshake. Of the 94 clients, 90 run and 80 complete a connection. Fourteen
rows carry a recorded harness limitation rather than an implementation fault:
ten `twisted-conch` clients (upstream `conch` opens `/dev/tty` and cannot be
driven non-interactively) and four `libssh2` clients (1.8.0, 1.8.1, 1.8.2 and
1.9.0 share no algorithm with the OpenSSH 10.3p1 used as the target).

The 370 are a sample, not the whole corpus: every family, every new version,
and both ends of each pre-existing range. Building all 1422 services would
take far longer than the disk the VM has.

## 7. Caveat: ports are derived from ordering

`templates/combined.yml.tpl` assigns host ports by enumeration index, so
adding a family renumbers every family sorted after it. Anything pinning a
port from the old `compose.yml` must be regenerated.
