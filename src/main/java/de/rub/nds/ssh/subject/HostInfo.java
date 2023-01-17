/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject;

import de.rub.nds.ssh.subject.constants.TransportType;

public class HostInfo {
    private String ip;
    private String hostname;
    private Integer port;
    private TransportType type;

    public HostInfo(String ip, String hostname, int port, TransportType type) {
        // called for SSH Clients
        // specifies where the client connects to
        this.ip = ip;
        this.hostname = hostname;
        this.port = port;
        this.type = type;
    }

    public HostInfo(String hostname, int port, TransportType transportType) {
        // called for SSH Servers
        // specifies where the server is available
        this.port = port;
        if (hostname == null) {
            this.hostname = "127.0.0.42";
            this.ip = "127.0.0.42";
        } else {
            this.hostname = hostname;
            this.ip = hostname;
        }

        this.type = transportType;
    }

    public TransportType getType() {
        return type;
    }

    public String getIp() {
        return ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void updatePort(int port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setType(TransportType type) {
        this.type = type;
    }
}
