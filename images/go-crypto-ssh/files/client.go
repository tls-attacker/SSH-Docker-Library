// Minimal SSH client built directly on golang.org/x/crypto/ssh.
package main

import (
	"flag"
	"fmt"
	"log"
	"os"

	"golang.org/x/crypto/ssh"
)

func main() {
	host := flag.String("h", "127.0.0.1", "host")
	port := flag.Int("p", 22, "port")
	user := flag.String("l", "sshattacker", "user")
	pass := flag.String("P", "secret", "password")
	key := flag.String("i", "", "private key file")
	cmd := flag.String("c", "true", "command to run")
	flag.Parse()

	cfg := &ssh.ClientConfig{
		User:            *user,
		HostKeyCallback: ssh.InsecureIgnoreHostKey(),
	}
	if *key != "" {
		pem, err := os.ReadFile(*key)
		if err != nil {
			log.Fatalf("read key: %v", err)
		}
		signer, err := ssh.ParsePrivateKey(pem)
		if err != nil {
			log.Fatalf("parse key: %v", err)
		}
		cfg.Auth = append(cfg.Auth, ssh.PublicKeys(signer))
	}
	cfg.Auth = append(cfg.Auth, ssh.Password(*pass))

	c, err := ssh.Dial("tcp", fmt.Sprintf("%s:%d", *host, *port), cfg)
	if err != nil {
		log.Fatalf("dial: %v", err)
	}
	defer c.Close()
	s, err := c.NewSession()
	if err != nil {
		log.Fatalf("session: %v", err)
	}
	defer s.Close()
	out, err := s.CombinedOutput(*cmd)
	fmt.Print(string(out))
	if err != nil {
		log.Printf("run: %v", err)
	}
}
