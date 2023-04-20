/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.rub.nds.ssh.subject.params;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBException;
import de.rub.nds.ssh.subject.SshImplementationType;
import de.rub.nds.ssh.subject.docker.DockerSshManagerFactory;
import org.junit.Test;
import de.rub.nds.ssh.subject.ConnectionRole;
import de.rub.nds.ssh.subject.docker.DockerExecInstance;
import de.rub.nds.ssh.subject.docker.DockerSshClientInstance;
import de.rub.nds.ssh.subject.report.ContainerReport;
import de.rub.nds.ssh.subject.report.InstanceContainer;

import static junit.framework.TestCase.assertTrue;

public class AvailableClientVersionsTest {

    // private static final String HOSTNAME = "nds.tls-docker-library-test.de";
    // when running the tests on another os than linux you might need to change the
    // ip use `docker run --rm -it alpine-build:3.12 ping -c1 host.docker.internal`
    // to find the correct IP
    private static final String IP = "172.26.0.1";
    private static final int PORT = 3022;
    private static final int CONNECTION_TIMEOUT = 10;

    @Test
    public void listAllClients() {
        System.out.println("Available Clients: ");
        for (SshImplementationType type : SshImplementationType.values()) {

            List<String> availableVersions = DockerSshManagerFactory.getAvailableVersions(ConnectionRole.CLIENT, type);
            System.out.println("Client version: " + type);
            for (String version : availableVersions) {
                System.out.println(version);
            }
        }
    }

    @Test
    public void serverTest() {

        System.out.println("Start Test Server \n");

        SshTestServer testServer = new SshTestServer(PORT, true);
        testServer.start();

        while (testServer.isStarted() != true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Server is running .... ");

        while (testServer.isStarted() != true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        boolean waiting = true;
        int timeout = 0;
        while (waiting && timeout < CONNECTION_TIMEOUT) {
            if (testServer.isConnectionSuccessful()) {
                waiting = false;
            }
            try {
                TimeUnit.SECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            timeout++;
        }

        try {
            testServer.stop("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void functionalSingleClient(SshImplementationType clientType, String version) {
        functionalSingleClient(clientType, version, false);
    }

    public void functionalSingleClient(SshImplementationType clientType, String version, Boolean debug) {
        System.out.println("Start Test Server \n");

        SshTestServer testServer = new SshTestServer(PORT, debug);
        testServer.start();

        while (testServer.isStarted() != true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Functional Clients: ");
        SshImplementationType type = clientType;

        try {
            boolean isFunctional = isFunctional(testServer, type, version);
            System.out.println(type.name() + ":" + version + " - " + isFunctional);
        } catch (Exception E) {
            E.printStackTrace();
            System.out.println(type.name() + ":" + version + "       ERROR");
        }

        try {
            testServer.stop("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void functionalClientParamiko() {
        functionalSingleClient(SshImplementationType.PARAMIKO, "2.11.0");
    }

    @Test
    public void functionalClientAllParamiko() {
        functionalClientsAllOfType(SshImplementationType.PARAMIKO);
    }

    @Test
    public void functionalClientOpenssh() {
        functionalSingleClient(SshImplementationType.OPENSSH, "8.8p1");
    }

    @Test
    public void functionalClientsAllOpenssh() {
        functionalClientsAllOfType(SshImplementationType.OPENSSH);
    }

    @Test
    public void functionalClientDropbear() {
        functionalSingleClient(SshImplementationType.DROPBEAR, "2013.60", true);
    }

    @Test
    public void functionalClientsAllDropbear() {
        functionalClientsAllOfType(SshImplementationType.DROPBEAR);
    }

    public void functionalClientsAllOfType(SshImplementationType type) {
        System.out.println("Start Test Server \n");
        SshTestServer testServer = new SshTestServer(PORT);
        testServer.start();

        while (testServer.isStarted() != true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Functional Clients: ");

        ContainerReport report = new ContainerReport();

        List<String> availableVersions = DockerSshManagerFactory.getAvailableVersions(ConnectionRole.CLIENT, type);
        for (String version : availableVersions) {
            try {
                boolean isFunctional = isFunctional(testServer, type, version);
                System.out.println(type.name() + ":" + version + " - " + isFunctional);
                report.addInstanceContainer(new InstanceContainer(ConnectionRole.CLIENT, type, version, isFunctional));
            } catch (Exception E) {
                E.printStackTrace();
                System.out.println(type.name() + ":" + version + "       ERROR");
            }
        }

        try {
            ContainerReport.write(new File("client_report_" + type + ".xml"), report);
            testServer.stop("");
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void functionalClientPuttyLinux() {
        functionalSingleClient(SshImplementationType.PUTTY, "0.76-2");
    }

    @Test
    public void functionalClientsAllPuttyLinux() {
        functionalClientsAllOfType(SshImplementationType.PUTTY);
    }

    @Test
    public void functionalClientGO() {
        functionalSingleClient(SshImplementationType.GO, "v0.0.0-20220924013350-4ba4fb4dd9e7");
    }

    @Test
    public void functionalClientAllGO() {
        functionalClientsAllOfType(SshImplementationType.GO);
    }

    @Test
    public void functionalClientAsyncssh() {
        functionalSingleClient(SshImplementationType.ASYNCSSH, "2.12.0");
    }

    @Test
    public void functionalClientAllAsyncssh() {
        functionalClientsAllOfType(SshImplementationType.ASYNCSSH);
    }

    @Test
    public void functionalClientZgrab2() {
        // work but not fire a command
        functionalSingleClient(SshImplementationType.ZGRAB2, "latest", true);
    }

    @Test
    public void functionalClientWolfSSH() {
        functionalSingleClient(SshImplementationType.WOLFSSH, "1.4.11-stable");
    }

    @Test
    public void functionalClientAllWolfSSH() {
        functionalClientsAllOfType(SshImplementationType.WOLFSSH);
    }

    @Test
    public void functionalClientMetasploit() {
        functionalSingleClient(SshImplementationType.METASPLOIT, "latest", false);
    }

    @Test
    public void functionalClientLibssh() {
        functionalSingleClient(SshImplementationType.LIBSSH, "0.9.5");
    }

    @Test
    public void functionalClientAllLibssh() {
        functionalClientsAllOfType(SshImplementationType.LIBSSH);
    }

    @Test
    public void functionalClientsTest() {

        System.out.println("Start Test Server \n");
        SshTestServer testServer = new SshTestServer(PORT);
        testServer.start();

        while (testServer.isStarted() != true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Functional Clients: ");

        ContainerReport report = new ContainerReport();
        for (SshImplementationType type : SshImplementationType.values()) {
            List<String> availableVersions = DockerSshManagerFactory.getAvailableVersions(ConnectionRole.CLIENT, type);

            for (String version : availableVersions) {
                try {
                    boolean isFunctional = isFunctional(testServer, type, version);
                    System.out.println(type.name() + ":" + version + " - " + isFunctional);
                    report.addInstanceContainer(
                        new InstanceContainer(ConnectionRole.CLIENT, type, version, isFunctional));
                } catch (Exception E) {
                    E.printStackTrace();
                    System.out.println(type.name() + ":" + version + "       ERROR");
                }
            }
        }

        try {
            ContainerReport.write(new File("client_report.xml"), report);
            testServer.stop("");
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void functionalClientsTestBreakOnFalse() {

        System.out.println("Start Test Server \n");
        SshTestServer testServer = new SshTestServer(PORT);
        testServer.start();

        while (testServer.isStarted() != true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Functional Clients: ");

        ContainerReport report = new ContainerReport();
        for (SshImplementationType type : SshImplementationType.values()) {
            List<String> availableVersions = DockerSshManagerFactory.getAvailableVersions(ConnectionRole.CLIENT, type);

            for (String version : availableVersions) {
                try {
                    boolean isFunctional = isFunctional(testServer, type, version);
                    System.out.println(type.name() + ":" + version + " - " + isFunctional);
                    report.addInstanceContainer(
                        new InstanceContainer(ConnectionRole.CLIENT, type, version, isFunctional));
                    if (!isFunctional) {
                        ContainerReport.write(new File("client_report.xml"), report);
                        testServer.stop("");
                    }
                    assertTrue(isFunctional);

                } catch (Exception E) {
                    E.printStackTrace();
                    System.out.println(type.name() + ":" + version + "       ERROR");
                }
            }
        }

        try {
            ContainerReport.write(new File("client_report.xml"), report);
            testServer.stop("");
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isFunctional(SshTestServer testServer, SshImplementationType type, String version) {
        DockerSshClientInstance client = null;
        DockerExecInstance ei = null;

        try {
            if (version == null || type == null) {
                System.out.println("Null: " + version);
                return false;
            }
            client = DockerSshManagerFactory.getSshClientBuilder(type, version).ip(IP).port(PORT)
                .connectOnStartup(false).insecureConnection(false).build();
            client.start();
            ei = (DockerExecInstance) client.connect();
            boolean waiting = true;
            int timeout = 0;
            while (waiting && timeout < CONNECTION_TIMEOUT) {
                if (testServer.isConnectionSuccessful()) {
                    waiting = false;
                }
                TimeUnit.SECONDS.sleep(3);
                timeout++;
            }

            boolean res = testServer.isConnectionSuccessful();
            testServer.setIsConnectionSuccessful(false);

            if (!res) {
                System.out.println("-Failed- Log:");
                for (String ln : ei.frameHandler.getLines()) {
                    System.out.println(ln);
                }
            }
            return res;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

}
