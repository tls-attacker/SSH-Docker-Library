/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject.properties;

import de.rub.nds.ssh.subject.ConnectionRole;
import de.rub.nds.ssh.subject.SshImplementationType;
import de.rub.nds.ssh.subject.exceptions.PropertyNotFoundException;
import java.util.LinkedList;
import java.util.List;

public class PropertyManager {

    private static PropertyManager instance;

    public static PropertyManager instance() {
        if (instance == null) {
            instance = new PropertyManager();
        }
        return instance;
    }

    private final List<ImageProperties> imagePropertyList;

    private static class Const {

        static final String CERT_KEY_PEM = "/cert/rsa2048key.pem";
        static final String CERT_CERT_PEM = "/cert/rsa2048cert.pem";
        static final String CERT_COMBINED_PEM = "/cert/rsa2048combined.pem";
        static final String RUST_TEST_CA_KEY = "/cert/test-ca/rsa/end.rsa";
        static final String RUST_TEST_CA_FULLCHAIN = "/cert/test-ca/rsa/end.fullchain";
        static final String CA_CERT = "/cert/ca.pem";
    }

    protected PropertyManager() {
        imagePropertyList = new LinkedList<>();

        imagePropertyList.add(
            new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.OPENSSH, "9.0p1", Const.CA_CERT, false));

        imagePropertyList.add(
            new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.PARAMIKO, "2.11.0", Const.CA_CERT, false));

        imagePropertyList.add(new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.DROPBEAR, "2022.82",
            Const.CA_CERT, false));

        imagePropertyList.add(new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.GO,
            "v0.0.0-20220924013350-4ba4fb4dd9e7", Const.CA_CERT, false));

        imagePropertyList.add(
            new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.PUTTY, "0.76-2", Const.CA_CERT, false));

        imagePropertyList.add(
            new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.ASYNCSSH, "2.12.0", Const.CA_CERT, false));

        imagePropertyList.add(
            new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.LIBSSH, "0.9.4", Const.CA_CERT, false));

        imagePropertyList.add(new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.WOLFSSH, "1.4.11-stable",
            Const.CA_CERT, false));

        imagePropertyList.add(
            new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.ZGRAB2, "latest", Const.CA_CERT, false));

        imagePropertyList.add(new ImageProperties(ConnectionRole.CLIENT, SshImplementationType.METASPLOIT, "latest",
            Const.CA_CERT, false));

    }

    public ImageProperties getProperties(ConnectionRole role, SshImplementationType type) {
        for (ImageProperties properties : imagePropertyList) {
            if (properties.getRole().equals(role)) {
                if (properties.getType().equals(type)) {
                    return properties;
                }
            }
        }
        throw new PropertyNotFoundException("No " + role.name() + " properties found for: " + type.name());
    }
}
