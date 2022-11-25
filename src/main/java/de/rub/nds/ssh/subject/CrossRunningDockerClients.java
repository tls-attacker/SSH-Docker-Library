/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject;

import de.rub.nds.ssh.subject.docker.DockerExecInstance;
import de.rub.nds.ssh.subject.docker.DockerSshClientInstance;
import de.rub.nds.ssh.subject.docker.DockerSshManagerFactory;
import de.rub.nds.ssh.subject.docker.DockerSshServerInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrossRunningDockerClients {

    private static final String IP = "127.0.0.1";
    private static final int SERVERPORT = 22510;
    private Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        new CrossRunningDockerClients().testWithFixedClient();
    }

    public CrossRunningDockerClients() {
    }

    public void testWithFixedClient() {
        DockerSshClientInstance client = null;
        DockerSshServerInstance server = null;

        try {
            client = DockerSshManagerFactory.getSshClientBuilder(SshImplementationType.OPENSSH, "8.8p1").ip(IP)
                .portToConnect(SERVERPORT).connectOnStartup(false).insecureConnection(false).build();
        } catch (InterruptedException E) {
            LOGGER.warn("Instance seems not runnable", E);
        }

        for (SshImplementationType type : SshImplementationType.values()) {
            List<String> availableVersions = DockerSshManagerFactory.getAvailableVersions(ConnectionRole.SERVER, type);
            for (String version : availableVersions) {
                try {
                    server = DockerSshManagerFactory.getSshServerBuilder(type, version).build(SERVERPORT);
                    server.start();
                    client.start();
                    DockerExecInstance ei = (DockerExecInstance) client.connect();
                    TimeUnit.SECONDS.sleep(3);
                    for (String ln : ei.frameHandler.getLines()) {
                        System.out.println(ln);
                    }
                } catch (InterruptedException E) {
                    LOGGER.warn("Instance seems not runnable", E);
                } finally {
                    if (server != null) {
                        server.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                }
            }
        }

    }
}
