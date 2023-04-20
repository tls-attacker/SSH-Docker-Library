/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject.exceptions;

public class ImplementationDidNotStartException extends RuntimeException {

    public ImplementationDidNotStartException() {
    }

    public ImplementationDidNotStartException(String message) {
        super(message);
    }

    public ImplementationDidNotStartException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImplementationDidNotStartException(Throwable cause) {
        super(cause);
    }

    public ImplementationDidNotStartException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
