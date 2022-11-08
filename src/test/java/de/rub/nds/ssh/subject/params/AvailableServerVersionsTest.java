/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject.params;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import javax.xml.bind.JAXBException;

import de.rub.nds.ssh.subject.ConnectionRole;
import de.rub.nds.ssh.subject.SshImplementationType;
import de.rub.nds.ssh.subject.docker.DockerSshManagerFactory;
import de.rub.nds.ssh.subject.docker.DockerSshServerInstance;
import de.rub.nds.ssh.subject.report.ContainerReport;
import de.rub.nds.ssh.subject.report.InstanceContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import org.junit.jupiter.api.Test;

public class AvailableServerVersionsTest {

    private Logger LOGGER = LogManager.getLogger();

    public AvailableServerVersionsTest() {
    }

    @Test
    public void listAllServers() {
        for (SshImplementationType type : SshImplementationType.values()) {
            List<String> availableVersions = DockerSshManagerFactory.getAvailableVersions(ConnectionRole.SERVER, type);
            System.out.println("Server version: " + type);
            for (String version : availableVersions) {
                System.out.println(version);
            }
        }
    }

    @Test
    public void testAllVersionsFunctional() throws JAXBException, IOException {
        Configurator.setRootLevel(org.apache.logging.log4j.Level.OFF);
        ContainerReport report = new ContainerReport();
        for (SshImplementationType type : SshImplementationType.values()) {
            List<String> availableVersions = DockerSshManagerFactory.getAvailableVersions(ConnectionRole.SERVER, type);
            for (String version : availableVersions) {
                try {
                    boolean isFunctional = isFunctional(type, version);
                    System.out.println(type.name() + ":" + version + " - " + isFunctional);
                    report.addInstanceContainer(
                        new InstanceContainer(ConnectionRole.SERVER, type, version, isFunctional));
                } catch (Exception E) {
                    E.printStackTrace();
                    System.out.println(type.name() + ":" + version + "       ERROR");
                }

            }
        }
        ContainerReport.write(new File("server_report.xml"), report);
    }

    public boolean isFunctional(SshImplementationType type, String version) {
        DockerSshServerInstance server = null;
        try {
            if (version == null || type == null) {
                System.out.println("Null: " + version);
                return false;
            }
            try {
                int inputPort = (int) Math.random() * 65000;
                server = DockerSshManagerFactory.getSshServerBuilder(type, version).build(inputPort);
                server.start();
            } catch (Exception E) {
                LOGGER.warn("Instance seems not runnable", E);
                return false;
            }
            // the hostname part might need some fixing.
            // On windows(wsl2) I needed the IP of the wsl vm here
            Socket socket = new Socket(server.getHostInfo().getHostname(), server.getPort());
            if (socket.isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (IOException ex) {
            return false;
        } finally {
            if (server != null) {
                server.close();
            }
        }
    }
}
