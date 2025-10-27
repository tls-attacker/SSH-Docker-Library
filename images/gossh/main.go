/**
Authored by ChatGPT based on paramiko-client-script.py
*/
package main

import (
	"flag"
	"fmt"
	"io"
	"log"
	"net"
	"os"
	"time"

	"golang.org/x/crypto/ssh"
)

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

	sshConfig := &ssh.ClientConfig{
		User:            username,
		Auth:            []ssh.AuthMethod{ssh.Password(password)},
		HostKeyCallback: ssh.InsecureIgnoreHostKey(),
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