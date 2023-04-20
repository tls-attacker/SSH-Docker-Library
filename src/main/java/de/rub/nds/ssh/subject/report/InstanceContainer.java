/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
/*
 */

package de.rub.nds.ssh.subject.report;

import de.rub.nds.ssh.subject.ConnectionRole;
import de.rub.nds.ssh.subject.SshImplementationType;

import java.io.Serializable;

/**
 *
 * @author robert
 */
public class InstanceContainer implements Serializable {

    private ConnectionRole role;

    private SshImplementationType implementationType;

    private String version;

    private boolean functional;

    private InstanceContainer() {
    }

    public InstanceContainer(ConnectionRole role, SshImplementationType implementationType, String version,
        boolean functional) {
        this.role = role;
        this.implementationType = implementationType;
        this.version = version;
        this.functional = functional;
    }

    public ConnectionRole getRole() {
        return role;
    }

    public void setRole(ConnectionRole role) {
        this.role = role;
    }

    public SshImplementationType getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(SshImplementationType implementationType) {
        this.implementationType = implementationType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isFunctional() {
        return functional;
    }

    public void setFunctional(boolean functional) {
        this.functional = functional;
    }
}
