/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject;

public enum SshImplementationType {
    OPENSSH,
    PARAMIKO,
    DROPBEAR,
    GO,
    PUTTY,
    ASYNCSSH,
    LIBSSH,
    WOLFSSH,
    ZGRAB2,
    METASPLOIT,
    SSHATTACKER;

    public static SshImplementationType fromString(String type) {
        for (SshImplementationType i : SshImplementationType.values()) {
            if (i.name().toLowerCase().equals(type.toLowerCase())) {
                return i;
            }
        }
        return null;
    }

}
