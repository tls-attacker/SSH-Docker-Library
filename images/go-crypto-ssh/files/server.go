// Minimal SSH server built directly on golang.org/x/crypto/ssh.
//
// The Go module is the implementation under test; this program is only the
// thin application around it, in the same spirit as the asyncssh image's
// simple_server.py.
package main

import (
	"flag"
	"fmt"
	"io"
	"log"
	"net"
	"os"

	"golang.org/x/crypto/ssh"
)

func loadAuthorized(path string) map[string]bool {
	out := map[string]bool{}
	data, err := os.ReadFile(path)
	if err != nil {
		return out
	}
	for len(data) > 0 {
		key, _, _, rest, err := ssh.ParseAuthorizedKey(data)
		if err != nil {
			break
		}
		out[string(key.Marshal())] = true
		data = rest
	}
	return out
}

func main() {
	addr := flag.String("addr", ":22", "listen address")
	hostKey := flag.String("hostkey", "/app/ssh_host_key", "host key file")
	authFile := flag.String("f", "/app/authorized_keys", "authorized_keys file")
	flag.Parse()

	user := os.Getenv("SSH_USERNAME")
	pass := os.Getenv("SSH_PASSWORD")
	authorized := loadAuthorized(*authFile)

	cfg := &ssh.ServerConfig{
		PasswordCallback: func(c ssh.ConnMetadata, p []byte) (*ssh.Permissions, error) {
			if c.User() == user && string(p) == pass {
				return nil, nil
			}
			return nil, fmt.Errorf("password rejected for %q", c.User())
		},
		PublicKeyCallback: func(c ssh.ConnMetadata, k ssh.PublicKey) (*ssh.Permissions, error) {
			if authorized[string(k.Marshal())] {
				return nil, nil
			}
			return nil, fmt.Errorf("key rejected for %q", c.User())
		},
	}

	pem, err := os.ReadFile(*hostKey)
	if err != nil {
		log.Fatalf("read host key: %v", err)
	}
	signer, err := ssh.ParsePrivateKey(pem)
	if err != nil {
		log.Fatalf("parse host key: %v", err)
	}
	cfg.AddHostKey(signer)

	ln, err := net.Listen("tcp", *addr)
	if err != nil {
		log.Fatalf("listen: %v", err)
	}
	log.Printf("listening on %s", *addr)
	for {
		c, err := ln.Accept()
		if err != nil {
			continue
		}
		go handle(c, cfg)
	}
}

func handle(c net.Conn, cfg *ssh.ServerConfig) {
	conn, chans, reqs, err := ssh.NewServerConn(c, cfg)
	if err != nil {
		c.Close()
		return
	}
	defer conn.Close()
	go ssh.DiscardRequests(reqs)
	for nc := range chans {
		if nc.ChannelType() != "session" {
			nc.Reject(ssh.UnknownChannelType, "only session channels")
			continue
		}
		ch, creqs, err := nc.Accept()
		if err != nil {
			continue
		}
		go func(ch ssh.Channel, creqs <-chan *ssh.Request) {
			defer ch.Close()
			for r := range creqs {
				switch r.Type {
				case "shell", "exec", "pty-req", "env", "window-change":
					if r.WantReply {
						r.Reply(true, nil)
					}
					if r.Type == "shell" || r.Type == "exec" {
						io.WriteString(ch, "ok\n")
						ch.SendRequest("exit-status", false, []byte{0, 0, 0, 0})
						return
					}
				default:
					if r.WantReply {
						r.Reply(false, nil)
					}
				}
			}
		}(ch, creqs)
	}
}
