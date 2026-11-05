<?php
// Minimal SSH client built on phpseclib.
//
// The 3.x and 4.x series ship different namespaces (phpseclib3\ and
// phpseclib4\), and this family covers both, so resolve it at runtime rather
// than pinning one.
require '/app/vendor/autoload.php';

$ns = class_exists('phpseclib4\\Net\\SSH2') ? 'phpseclib4' : 'phpseclib3';
$SSH2 = $ns . '\\Net\\SSH2';
$Loader = $ns . '\\Crypt\\PublicKeyLoader';

$opts = getopt('h:p:l:P:i:c:');
$host = $opts['h'] ?? '127.0.0.1';
$port = (int)($opts['p'] ?? 22);
$user = $opts['l'] ?? 'sshattacker';
$cmd  = $opts['c'] ?? 'true';

$ssh = new $SSH2($host, $port);
$cred = isset($opts['i'])
      ? $Loader::load(file_get_contents($opts['i']))
      : ($opts['P'] ?? 'secret');
if (!$ssh->login($user, $cred)) {
    fwrite(STDERR, "login failed\n");
    exit(1);
}
echo $ssh->getServerIdentification(), "\n";
echo $ssh->exec($cmd);
