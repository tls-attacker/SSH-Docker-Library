/**
Authored by ChatGPT based on paramiko-client-script.py
*/
package main

import (
	"errors"
	"flag"
	"fmt"
	"io"
	"log"
	"net"
	"os"
	"path/filepath"
	"time"

	"golang.org/x/crypto/ssh"
	"golang.org/x/crypto/ssh/knownhosts"
)

// hostKeyCallback trusts the keys in ~/.ssh/known_hosts and treats unknown keys as SSLC_HOSTKEY_PROMPT says: accept
// (and append them to known_hosts), accept_once or reject. A key that differs from a known one for the host is
// rejected either way. Without SSLC_HOSTKEY_PROMPT, host keys are not checked at all, as before.
func hostKeyCallback() (ssh.HostKeyCallback, error) {
	prompt, set := os.LookupEnv("SSLC_HOSTKEY_PROMPT")
	if !set {
		return ssh.InsecureIgnoreHostKey(), nil
	}
	if prompt != "accept" && prompt != "accept_once" && prompt != "reject" {
		return nil, fmt.Errorf("SSLC_HOSTKEY_PROMPT=%s is not supported by gossh", prompt)
	}
	home, err := os.UserHomeDir()
	if err != nil {
		return nil, err
	}
	path := filepath.Join(home, ".ssh", "known_hosts")
	var known ssh.HostKeyCallback
	if _, err := os.Stat(path); err == nil {
		if known, err = knownhosts.New(path); err != nil {
			return nil, err
		}
	}
	return func(hostname string, remote net.Addr, key ssh.PublicKey) error {
		if known != nil {
			err := known(hostname, remote, key)
			var keyErr *knownhosts.KeyError
			// a known key, a changed one (Want lists the known keys) or a broken file
			if err == nil || !errors.As(err, &keyErr) || len(keyErr.Want) > 0 {
				return err
			}
		}
		switch prompt {
		case "reject":
			return fmt.Errorf("host key of %s is unknown", hostname)
		case "accept":
			if err := os.MkdirAll(filepath.Dir(path), 0o700); err != nil {
				return err
			}
			file, err := os.OpenFile(path, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0o644)
			if err != nil {
				return err
			}
			defer file.Close()
			_, err = fmt.Fprintln(file, knownhosts.Line([]string{knownhosts.Normalize(hostname)}, key))
			return err
		}
		return nil
	}, nil
}

func main() {
	var host string
	var port int
	var username string
	var password string
	var command string
	var showOut bool
	var showErr bool

	flag.StringVar(&host, "host", "127.0.0.1", "hostname or ip")
	flag.IntVar(&port, "port", 22, "port")
	flag.StringVar(&username, "username", "sshattacker", "username")
	flag.StringVar(&password, "password", "secret", "password")
	flag.StringVar(&command, "command", "pwd", "command")
	flag.BoolVar(&showOut, "output", false, "print output")
	flag.BoolVar(&showErr, "error", false, "print error")
	flag.Parse()

	if showOut {
		fmt.Println("init Client")
	}

	addr := fmt.Sprintf("%s:%d", host, port)
	fmt.Printf("Trying to connect to: [host=%s, port=%d, username=%s, password=%s]\n",
		host, port, username, password)

	checkHostKey, err := hostKeyCallback()
	if err != nil {
		log.Fatalf("host key setup failed: %v", err)
	}
	sshConfig := &ssh.ClientConfig{
		User:            username,
		Auth:            []ssh.AuthMethod{ssh.Password(password)},
		HostKeyCallback: checkHostKey,
		Timeout:         10 * time.Second,
	}

	// Dial TCP first to get clearer error messages on connection issues (optional)
	conn, err := net.DialTimeout("tcp", addr, 10*time.Second)
	if err != nil {
		log.Fatalf("tcp connect failed: %v", err)
	}
	defer conn.Close()

	ncc, chans, reqs, err := ssh.NewClientConn(conn, addr, sshConfig)
	if err != nil {
		log.Fatalf("ssh handshake failed: %v", err)
	}
	client := ssh.NewClient(ncc, chans, reqs)
	defer client.Close()

	if showOut {
		fmt.Println("start command")
	}
	session, err := client.NewSession()
	if err != nil {
		log.Fatalf("failed to create session: %v", err)
	}
	defer session.Close()

	stdoutPipe, err := session.StdoutPipe()
	if err != nil {
		log.Fatalf("stdout pipe: %v", err)
	}
	stderrPipe, err := session.StderrPipe()
	if err != nil {
		log.Fatalf("stderr pipe: %v", err)
	}

	if err := session.Start(command); err != nil {
		log.Fatalf("failed to start command: %v", err)
	}

	outBytes, _ := io.ReadAll(stdoutPipe)
	errBytes, _ := io.ReadAll(stderrPipe)

	if err := session.Wait(); err != nil {
		// Don't always treat it as fatal; we still want to show captured stderr if requested
		if showErr {
			fmt.Println("error: ", string(errBytes))
		}
		log.Printf("command finished with error: %v", err)
		os.Exit(1)
	}

	if showOut {
		fmt.Println("finish")
		fmt.Println("output: ", string(outBytes))
	} else {
		// If not showOut but user requested only stdout/err flags, still print accordingly:
		if showOut {
			fmt.Println(string(outBytes))
		}
		if showErr {
			fmt.Println("error: ", string(errBytes))
		}
	}
}