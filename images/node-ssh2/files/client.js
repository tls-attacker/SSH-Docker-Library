// Minimal SSH client built on the ssh2 npm module.
'use strict';
const fs = require('fs');
const { Client } = require('ssh2');

const argv = require('process').argv.slice(2);
function opt(flag, dflt) {
  const i = argv.indexOf(flag);
  return i >= 0 ? argv[i + 1] : dflt;
}

const cfg = {
  host: opt('-h', '127.0.0.1'),
  port: parseInt(opt('-p', '22'), 10),
  username: opt('-l', 'sshattacker'),
};
const key = opt('-i', null);
if (key) cfg.privateKey = fs.readFileSync(key); else cfg.password = opt('-P', 'secret');

const c = new Client();
c.on('ready', () => {
  c.exec(opt('-c', 'true'), (err, stream) => {
    if (err) { console.error(err.message); process.exit(1); }
    stream.on('data', (d) => process.stdout.write(d));
    stream.on('close', () => c.end());
  });
}).on('error', (e) => { console.error(e.message); process.exit(1); }).connect(cfg);
