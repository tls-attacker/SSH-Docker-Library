// Minimal SSH server built on the ssh2 npm module.
'use strict';
const fs = require('fs');
const { timingSafeEqual } = require('crypto');
const { Server, utils } = require('ssh2');

const USER = process.env.SSH_USERNAME || 'sshattacker';
const PASS = process.env.SSH_PASSWORD || 'secret';

const authorized = fs.readFileSync('/app/authorized_keys', 'utf8')
  .split('\n').filter((l) => l.trim())
  .map((l) => { try { return utils.parseKey(l); } catch (e) { return null; } })
  .filter(Boolean);

function eq(a, b) {
  const ba = Buffer.from(a), bb = Buffer.from(b);
  return ba.length === bb.length && timingSafeEqual(ba, bb);
}

new Server({ hostKeys: [fs.readFileSync('/app/ssh_host_key')] }, (client) => {
  client.on('authentication', (ctx) => {
    if (ctx.method === 'password' && eq(ctx.username, USER) && eq(ctx.password, PASS)) {
      return ctx.accept();
    }
    if (ctx.method === 'publickey') {
      for (const k of authorized) {
        if (ctx.key.algo === k.type && eq(ctx.key.data, k.getPublicSSH())) {
          if (!ctx.signature) return ctx.accept();
          if (k.verify(ctx.blob, ctx.signature, ctx.hashAlgo) === true) return ctx.accept();
        }
      }
    }
    return ctx.reject();
  });
  client.on('ready', () => {
    client.on('session', (accept) => {
      const session = accept();
      const finish = (stream) => {
        stream.write('ok\n');
        stream.exit(0);
        stream.end();
      };
      session.on('exec', (acc) => finish(acc()));
      session.on('shell', (acc) => finish(acc()));
      session.on('pty', (acc) => acc && acc());
    });
  });
  client.on('error', () => {});
}).listen(22, '0.0.0.0', function () {
  console.log('listening on ' + this.address().port);
});
