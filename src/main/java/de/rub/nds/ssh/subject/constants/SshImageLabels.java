/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject.constants;

public enum SshImageLabels {
    IMPLEMENTATION("ssh_implementation"),
    VERSION("ssh_implementation_version"),
    CONNECTION_ROLE("ssh_implementation_connectionRole");

    private String labelName;

    SshImageLabels(String label) {
        this.labelName = label;
    }

    public String getLabelName() {
        return this.labelName;
    }
}
