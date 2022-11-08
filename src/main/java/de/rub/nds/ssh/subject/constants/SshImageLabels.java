/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject.constants;

public enum SshImageLabels {
    IMPLEMENTATION("ssh.implementation.name"),
    VERSION("ssh.implementation.version"),
    TYPE("ssh.implementation.type");

    private String labelName;

    SshImageLabels(String label) {
        this.labelName = label;
    }

    public String getLabelName() {
        return this.labelName;
    }
}
