/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject.exceptions;

public class SshVersionNotFoundException extends RuntimeException {

    public SshVersionNotFoundException() {
    }

    public SshVersionNotFoundException(String message) {
        super(message);
    }

    public SshVersionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SshVersionNotFoundException(Throwable cause) {
        super(cause);
    }

    public SshVersionNotFoundException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
