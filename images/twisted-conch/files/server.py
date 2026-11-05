"""Minimal SSH server built on Twisted Conch.

Twisted is the implementation under test; this script is only the thin
application around it.  OpenSSH's compatibility table carries two rows for
Twisted banners, which is why this family is worth having in the corpus.
"""
import os

from twisted.conch import avatar
from twisted.conch.checkers import InMemorySSHKeyDB, SSHPublicKeyChecker
from twisted.conch.ssh import connection, factory, keys, session, userauth
from twisted.conch.ssh.transport import SSHServerTransport
from twisted.cred import portal
from twisted.cred.checkers import InMemoryUsernamePasswordDatabaseDontUse
from twisted.internet import protocol, reactor
from zope.interface import implementer

USER = os.environ.get("SSH_USERNAME", "sshattacker").encode()
PASS = os.environ.get("SSH_PASSWORD", "secret").encode()


class _Protocol(protocol.Protocol):
    def connectionMade(self):
        self.transport.write(b"ok\r\n")
        self.transport.loseConnection()


@implementer(session.ISession)
class _Session:
    def __init__(self, avatar):
        pass

    def getPty(self, term, windowSize, attrs):
        pass

    def execCommand(self, proto, cmd):
        p = _Protocol()
        p.makeConnection(proto)
        proto.makeConnection(session.wrapProtocol(p))

    def openShell(self, proto):
        self.execCommand(proto, b"")

    def eofReceived(self):
        pass

    def closed(self):
        pass


class _Avatar(avatar.ConchUser):
    def __init__(self, username):
        avatar.ConchUser.__init__(self)
        self.username = username
        self.channelLookup.update({b"session": session.SSHSession})


@implementer(portal.IRealm)
class _Realm:
    def requestAvatar(self, avatarId, mind, *interfaces):
        return interfaces[0], _Avatar(avatarId), lambda: None


def main():
    with open("/app/ssh_host_key", "rb") as fh:
        priv = keys.Key.fromString(fh.read())
    with open("/app/ssh_host_key.pub", "rb") as fh:
        pub = keys.Key.fromString(fh.read())

    authorized = []
    with open("/app/authorized_keys", "rb") as fh:
        for line in fh:
            line = line.strip()
            if not line:
                continue
            try:
                authorized.append(keys.Key.fromString(line))
            except Exception:
                pass

    f = factory.SSHFactory()
    f.privateKeys = {priv.sshType(): priv}
    f.publicKeys = {pub.sshType(): pub}
    f.portal = portal.Portal(_Realm())
    f.portal.registerChecker(
        InMemoryUsernamePasswordDatabaseDontUse(**{USER.decode(): PASS})
    )
    f.portal.registerChecker(
        SSHPublicKeyChecker(InMemorySSHKeyDB({USER: authorized}))
    )
    f.services = {
        b"ssh-userauth": userauth.SSHUserAuthServer,
        b"ssh-connection": connection.SSHConnection,
    }
    reactor.listenTCP(int(os.environ.get("SSH_PORT", "22")), f)
    print("listening")
    reactor.run()


if __name__ == "__main__":
    main()
