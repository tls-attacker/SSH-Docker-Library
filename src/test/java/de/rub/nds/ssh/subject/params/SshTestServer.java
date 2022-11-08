/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
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

import org.apache.sshd.common.kex.BuiltinDHFactories;
import org.apache.sshd.common.kex.KexProposalOption;
import org.apache.sshd.common.kex.KeyExchangeFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.keyprovider.MappedKeyPairProvider;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.logging.AbstractLoggingBean;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.StaticPublickeyAuthenticator;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.AbstractCommandSupport;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.kex.DHGServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ShellFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;

public class SshTestServer extends Thread {

    private final int port;
    private Boolean debug = false;
    private boolean isServerDone = false;
    private boolean isConnectionSuccessful = false;
    private static final String PATH_TO_KEYSTORE = "./certs/keys.jks";
    private static final String KEYSTORE_PASSWORD = "password";
    private SshServer server = null;

    SshTestServer(int port) {
        this.port = port;
    }

    SshTestServer(int port, Boolean debug) {
        this.debug = debug;
        this.port = port;
    }

    private class AcceptAllPasswordAuthenticator extends AbstractLoggingBean implements PasswordAuthenticator {

        public AcceptAllPasswordAuthenticator() {
            super();
        }

        @Override
        public boolean authenticate(String username, String password, ServerSession session) {
            return true;
        }
    }

    private class AcceptAllPublickeyAuthenticator extends StaticPublickeyAuthenticator {
        private AcceptAllPublickeyAuthenticator() {
            super(true);
        }
    }

    private class EchoShell extends CommandExecutionHelper {
        public EchoShell(String command) {
            super(command);
        }

        public EchoShell() {
            super();
        }

        @Override
        protected boolean handleCommandLine(String command) throws Exception {
            OutputStream out = getOutputStream();
            out.write((command + "\n").getBytes(StandardCharsets.UTF_8));
            out.write(("# : \n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            isConnectionSuccessful = true;
            return true;
        }

        @Override
        public void destroy(ChannelSession channel) throws Exception {
            super.destroy(channel);
        }
    }

    private abstract class CommandExecutionHelper extends AbstractCommandSupport {
        protected CommandExecutionHelper() {
            this(null);
        }

        protected CommandExecutionHelper(String command) {
            super(command, null);
        }

        @Override
        public void run() {
            ;
            String command = getCommand();
            try {
                if (command == null) {
                    BufferedWriter w =
                        new BufferedWriter(new OutputStreamWriter(getOutputStream(), StandardCharsets.UTF_8));
                    w.write("Hallo Test server here! \n");
                    w.write("# : ");
                    w.flush();

                    try (BufferedReader r =
                        new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8))) {
                        for (;;) {
                            command = r.readLine();
                            if (!handleCommandLine(command)) {
                                return;
                            }
                        }
                    }
                } else {
                    handleCommandLine(command);
                }
            } catch (InterruptedIOException e) {
                // Ignore - signaled end
            } catch (Exception e) {
                String message =
                    "Failed (" + e.getClass().getSimpleName() + ") to handle '" + command + "': " + e.getMessage();
                try {
                    OutputStream stderr = getErrorStream();
                    stderr.write(message.getBytes(StandardCharsets.US_ASCII));
                } catch (IOException ioe) {
                    log.warn("Failed ({}) to write error message={}: {}", e.getClass().getSimpleName(), message,
                        ioe.getMessage());
                } finally {
                    onExit(-1, message);
                }
            } finally {
                onExit(0);
            }
        }

        /**
         * @param  command
         *                   The command line
         * @return           {@code true} if continue accepting command
         * @throws Exception
         *                   If failed to handle the command line
         */
        protected abstract boolean handleCommandLine(String command) throws Exception;
    }

    private SshServer createSshServer() {

        SshServer server = SshServer.setUpDefaultServer();
        if (debug) {
            server.addSessionListener(new SessionListener() {
                @Override
                public void sessionEstablished(Session session) {
                    System.out.println("SESSION ESTABLISHED");
                }

                @Override
                public void sessionCreated(Session session) {
                    System.out.println("SESSION CREATED");
                }

                @Override
                public void sessionPeerIdentificationSend(Session session, String version, List<String> extraLines) {
                    System.out.println("SESSION PeerIdentificationSend version:" + version + " extraLines");
                }

                @Override
                public void sessionPeerIdentificationLine(Session session, String line, List<String> extraLines) {
                    System.out
                        .println("SESSION sessionPeerIdentificationLine line:" + line + " extralines:" + extraLines);
                }

                @Override
                public void sessionPeerIdentificationReceived(Session session, String version,
                    List<String> extraLines) {
                    System.out.println(
                        "SESSION sessionPeerIdentificationReceived version:" + version + " extralines:" + extraLines);
                }

                @Override
                public void sessionNegotiationOptionsCreated(Session session, Map<KexProposalOption, String> proposal) {
                    System.out.println("SESSION sessionNegotiationOptionsCreated  KexProposalOption" + proposal);
                }

                @Override
                public void sessionNegotiationStart(Session session, Map<KexProposalOption, String> clientProposal,
                    Map<KexProposalOption, String> serverProposal) {
                    System.out.println("SESSION sessionNegotiationStart clientProposal" + clientProposal
                        + " serverProposal " + serverProposal);
                }

                @Override
                public void sessionNegotiationEnd(Session session, Map<KexProposalOption, String> clientProposal,
                    Map<KexProposalOption, String> serverProposal, Map<KexProposalOption, String> negotiatedOptions,
                    Throwable reason) {
                    System.out.println("SESSION sessionNegotiationEnd clientProposal:" + clientProposal
                        + " serverProposal:" + serverProposal + " negotiatedOptions:" + negotiatedOptions);
                }

                @Override
                public void sessionEvent(Session session, Event event) {
                    System.out.println("SESSION sessionEvent event:" + event);
                }

                @Override
                public void sessionException(Session session, Throwable t) {
                    System.out.println("SESSION sessionException");
                }

                @Override
                public void sessionDisconnect(Session session, int reason, String msg, String language,
                    boolean initiator) {
                    System.out
                        .println("SESSION sessionDisconnect reason" + reason + " msg:" + msg + " language:" + language);
                }

                @Override
                public void sessionClosed(Session session) {
                    System.out.println("SESSION sessionClosed");
                }
            });
        }
        server.setPort(port);
//        SimpleGeneratorHostKeyProvider hostKeyProvider_ssh_rsa =
//                new SimpleGeneratorHostKeyProvider(new File("hostkey_ssh_rsa.ser").toPath());
        SimpleGeneratorHostKeyProvider hostKeyProvider_ecdsa_sha2_nistp521 =
            new SimpleGeneratorHostKeyProvider(new File("hostkey.ser_ecdsa_sha2_nistp521").toPath());
//        SimpleGeneratorHostKeyProvider hostKeyProvider_ssh_ed25519 =
//                new SimpleGeneratorHostKeyProvider(new File("hostkey.ssh_ed25519").toPath());
//        SimpleGeneratorHostKeyProvider hostKeyProvider_ecdsa_sha2_nistp256 =
//                new SimpleGeneratorHostKeyProvider(new File("hostkey.").toPath());

        MappedKeyPairProvider provider;
        try {
            KeyPair ec521 = hostKeyProvider_ecdsa_sha2_nistp521.loadKey(null, KeyPairProvider.ECDSA_SHA2_NISTP521);
//            KeyPair ed25519 = hostKeyProvider_ssh_ed25519.loadKey(null,KeyPairProvider.SSH_ED25519);
//            KeyPair rsa = hostKeyProvider_ssh_rsa.loadKey(null,KeyPairProvider.SSH_RSA);
//            provider = new MappedKeyPairProvider(ec521,rsa);

            provider = new MappedKeyPairProvider(SecurityUtils.getKeyPairGenerator("RSA").generateKeyPair(), ec521);

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.setKeyPairProvider(provider);
        server.setPublickeyAuthenticator(new AcceptAllPublickeyAuthenticator());
        server.setPasswordAuthenticator(new AcceptAllPasswordAuthenticator());
        server.setShellFactory(new ShellFactory() {
            @Override
            public Command createShell(ChannelSession channel) throws IOException {
                return new EchoShell();
            }
        });
        server.setCommandFactory(new CommandFactory() {

            @Override
            public Command createCommand(ChannelSession channel, String command) throws IOException {
                return new EchoShell(command);
            }
        });

        List<KeyExchangeFactory> kexExchangeFactories = server.getKeyExchangeFactories();
        kexExchangeFactories.add(DHGServer.newFactory(BuiltinDHFactories.dhg1));
        server.setKeyExchangeFactories(kexExchangeFactories);

        return server;
    }

    @Override
    public void run() {
        server = createSshServer();
        try {
            server.start();
            while (!isServerDone) {
            }
            server.stop();

        } catch (IOException | RuntimeException e) {

        }
    }

    public int getPort() {
        return port;
    }

    public boolean isServerDone() {
        return isServerDone;
    }

    public boolean isStarted() {
        if (server != null) {
            return server.isStarted();
        } else {
            return false;
        }
    }

    public void stop(String host) throws IOException {
        this.isServerDone = true;
    }

    public void setIsConnectionSuccessful(boolean isConnectionSuccessful) {
        this.isConnectionSuccessful = isConnectionSuccessful;
    }

    public boolean isConnectionSuccessful() {
        return isConnectionSuccessful;
    }
}
