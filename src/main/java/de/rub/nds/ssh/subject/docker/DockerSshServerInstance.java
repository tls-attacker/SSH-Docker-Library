/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject.docker;

import java.util.function.UnaryOperator;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.api.model.Ports.Binding;

import de.rub.nds.ssh.subject.HostInfo;
import de.rub.nds.ssh.subject.constants.SshImageLabels;
import de.rub.nds.ssh.subject.params.ParameterProfile;
import de.rub.nds.ssh.subject.properties.ImageProperties;
import de.rub.nds.ssh.subject.ConnectionRole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DockerSshServerInstance extends DockerSshInstance {

    private static Logger LOGGER = LogManager.getLogger();

    private int port;
    private final HostInfo hostInfo;
    private final String additionalParameters;
    private final boolean parallelize;
    private final boolean insecureConnection;
    private ExposedPort exposedImplementationPort;

    public DockerSshServerInstance(String containerName, ParameterProfile profile, ImageProperties imageProperties,
        String version, boolean autoRemove, HostInfo hostInfo, String additionalParameters, boolean parallelize,
        boolean insecureConnection, UnaryOperator<HostConfig> hostConfigHook) {
        super(containerName, profile, imageProperties, version, ConnectionRole.SERVER, autoRemove, hostConfigHook);
        this.port = hostInfo.getPort(); // fill with default port
        this.hostInfo = hostInfo;
        this.additionalParameters = additionalParameters;
        this.parallelize = parallelize;
        this.insecureConnection = insecureConnection;
    }

    @Override
    protected HostConfig prepareHostConfig(HostConfig cfg) {
        return super.prepareHostConfig(cfg)
            .withPortBindings(new PortBinding(Binding.empty(),
                new ExposedPort(imageProperties.getInternalPort(), hostInfo.getType().toInternetProtocol())))
            .withReadonlyRootfs(true);
    }

    @Override
    protected CreateContainerCmd prepareCreateContainerCmd(CreateContainerCmd cmd) {
        String host;
        if (hostInfo.getHostname() == null || imageProperties.isUseIP()) {
            host = hostInfo.getIp();
        } else {
            host = hostInfo.getHostname();
        }
        ExposedPort tcp22 = ExposedPort.tcp(22);
        Ports portBindings = new Ports();
        portBindings.bind(tcp22, Ports.Binding.bindPort(hostInfo.getPort()));
        exposedImplementationPort = new ExposedPort(22, hostInfo.getType().toInternetProtocol());
        // ToDO check if mounting is needed

        HostConfig hcfg = HostConfig.newHostConfig();
        if (super.hostConfigHook != null) {
            hcfg = hostConfigHook.apply(hcfg);
        }
        hcfg.withPortBindings(portBindings);
        return cmd.withAttachStderr(true).withAttachStdout(true).withAttachStdin(true).withTty(true).withStdInOnce(true)
            .withStdinOpen(true).withExposedPorts(exposedImplementationPort).withHostConfig(hcfg);
    }

    @Override
    public void start() {
        super.start();
        updateInstancePort();

        LOGGER.debug("Successfully started container with ID:" + containerId + " as: "
            + image.getLabels().get(SshImageLabels.IMPLEMENTATION.getLabelName()) + "-"
            + image.getLabels().get(SshImageLabels.TYPE.getLabelName()) + "-"
            + image.getLabels().get(SshImageLabels.VERSION.getLabelName()) + " running on port: " + port);
    }

    /**
     * Update port to match actually exposed port.
     */
    public void updateInstancePort() {
        InspectContainerResponse containerInfo = DOCKER.inspectContainerCmd(getId()).exec();
        if (containerInfo == null) {
            throw new IllegalStateException("Could not find container with ID:" + getId());
        }
        NetworkSettings networkSettings = containerInfo.getNetworkSettings();
        if (networkSettings == null) {
            throw new IllegalStateException(
                "Cannot retrieve InstacePort, Network not properly configured for container with ID:" + getId());
        }
        if (exposedImplementationPort == null) {
            throw new IllegalStateException(
                "Unable to update port - no exposed port set for container with ID:" + getId());
        }

        Binding[] binding = networkSettings.getPorts().getBindings().get(exposedImplementationPort);
        if (binding != null) {
            // only update if port mapping was necessary
            port = Integer.valueOf(binding[0].getHostPortSpec());
        }
    }

    public int getPort() {
        return port;
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }
}