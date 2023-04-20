/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject.docker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;

import de.rub.nds.ssh.subject.HostInfo;
import de.rub.nds.ssh.subject.constants.SshImageLabels;
import de.rub.nds.ssh.subject.constants.TransportType;
import de.rub.nds.ssh.subject.exceptions.DefaultProfileNotFoundException;
import de.rub.nds.ssh.subject.exceptions.PropertyNotFoundException;
import de.rub.nds.ssh.subject.params.ParameterProfile;
import de.rub.nds.ssh.subject.params.ParameterProfileManager;
import de.rub.nds.ssh.subject.properties.ImageProperties;
import de.rub.nds.ssh.subject.properties.PropertyManager;
import de.rub.nds.ssh.subject.ConnectionRole;
import de.rub.nds.ssh.subject.SshImplementationType;

/**
 * Creates SSH-Server or SSH-Client Instances as Docker Container Holds the Config for each SSH-Server or SSH-Client
 */
public class DockerSshManagerFactory {
    private DockerSshManagerFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final com.github.dockerjava.api.DockerClient DOCKER = DockerClientManager.getDockerClient();

    private static final int DEFAULT_PORT = 4433;

    @SuppressWarnings("unchecked")
    public abstract static class SshInstanceBuilder<T extends SshInstanceBuilder<T>> {
        protected final ParameterProfile profile;
        protected final ImageProperties imageProperties;
        protected final String version;
        protected boolean autoRemove = true;
        // shared constructor parameters
        // Host Info:
        protected final TransportType transportType;
        protected String ip = null;
        protected String hostname = null;
        protected int port = DEFAULT_PORT;
        protected UnaryOperator<HostConfig> hostConfigHook;
        // remaining shared params
        protected String additionalParameters = null;
        protected boolean parallelize = false;
        protected boolean insecureConnection = false;
        protected String containerName;

        public SshInstanceBuilder(SshImplementationType type, String version, ConnectionRole role,
            TransportType transportType) {
            this.profile = retrieveParameterProfile(type, version, role);
            this.imageProperties = retrieveImageProperties(role, type);
            this.version = version;
            this.transportType = transportType;
        }

        public T autoRemove(boolean value) {
            autoRemove = value;
            return (T) this;
        }

        public T ip(String value) {
            ip = value;
            return (T) this;
        }

        public T hostname(String value) {
            hostname = value;
            return (T) this;
        }

        public T containerName(String value) {
            containerName = value;
            return (T) this;
        }

        public T port(int value) {
            port = value;
            return (T) this;
        }

        public T additionalParameters(String value) {
            additionalParameters = value;
            return (T) this;
        }

        public T parallelize(boolean value) {
            parallelize = value;
            return (T) this;
        }

        public T insecureConnection(boolean value) {
            insecureConnection = value;
            return (T) this;
        }

        public T hostConfigHook(UnaryOperator<HostConfig> value) {
            hostConfigHook = value;
            return (T) this;
        }

        public abstract DockerSshInstance build() throws DockerException, InterruptedException;
    }

    public static class SshClientInstanceBuilder extends SshInstanceBuilder<SshClientInstanceBuilder> {

        protected boolean connectOnStartup = true;

        public SshClientInstanceBuilder(SshImplementationType type, String version, TransportType transportType) {
            super(type, version, ConnectionRole.CLIENT, transportType);
        }

        @Override
        public DockerSshClientInstance build() throws DockerException, InterruptedException {
            return new DockerSshClientInstance(containerName, profile, imageProperties, version, autoRemove,
                new HostInfo(ip, hostname, port, transportType), additionalParameters, parallelize, insecureConnection,
                connectOnStartup, hostConfigHook);
        }

        public SshClientInstanceBuilder connectOnStartup(boolean value) {
            connectOnStartup = value;
            return this;
        }

    }

    public static class SshServerInstanceBuilder extends SshInstanceBuilder<SshServerInstanceBuilder> {

        public SshServerInstanceBuilder(SshImplementationType type, String version, TransportType transportType) {
            super(type, version, ConnectionRole.SERVER, transportType);
        }

        @Override
        public DockerSshServerInstance build() throws DockerException, InterruptedException {
            return new DockerSshServerInstance(containerName, profile, imageProperties, version, autoRemove,
                new HostInfo(ip, hostname, port, transportType), additionalParameters, parallelize, insecureConnection,
                hostConfigHook);
        }

    }

    public static SshClientInstanceBuilder getSshClientBuilder(SshImplementationType type, String version) {
        return new SshClientInstanceBuilder(type, version, TransportType.TCP);
    }

    public static SshClientInstanceBuilder getDSshClientBuilder(SshImplementationType type, String version) {
        return new SshClientInstanceBuilder(type, version, TransportType.UDP);
    }

    public static SshServerInstanceBuilder getSshServerBuilder(SshImplementationType type, String version) {
        return new SshServerInstanceBuilder(type, version, TransportType.TCP);
    }

    public static SshServerInstanceBuilder getDSshServerBuilder(SshImplementationType type, String version) {
        return new SshServerInstanceBuilder(type, version, TransportType.UDP);
    }

    public static boolean clientExists(SshImplementationType type, String version) {
        return checkExists(type, version, ConnectionRole.CLIENT);
    }

    public static boolean serverExists(SshImplementationType type, String version) {
        return checkExists(type, version, ConnectionRole.SERVER);
    }

    private static boolean checkExists(SshImplementationType type, String version, ConnectionRole role) {
        return PropertyManager.instance().getProperties(role, type) != null
            && ParameterProfileManager.instance().getProfile(type, version, role) != null;
    }

    public static ImageProperties retrieveImageProperties(ConnectionRole role, SshImplementationType type)
        throws PropertyNotFoundException {
        ImageProperties properties = PropertyManager.instance().getProperties(role, type);
        if (properties == null) {
            throw new PropertyNotFoundException("Could not find a Property for " + role.name() + ": " + type.name());
        }
        return properties;
    }

    public static ParameterProfile retrieveParameterProfile(SshImplementationType type, String version,
        ConnectionRole role) throws DefaultProfileNotFoundException {
        ParameterProfile profile = ParameterProfileManager.instance().getProfile(type, version, role);
        if (profile == null) {
            throw new DefaultProfileNotFoundException(
                "Could not find a Profile for " + role.name() + ": " + type.name() + ":" + version);
        }
        return profile;
    }

    public static List<String> getAvailableVersions(ConnectionRole role, SshImplementationType type) {
        List<String> versionList = new LinkedList<>();
        Map<String, String> labels = new HashMap<>();
        labels.put(SshImageLabels.IMPLEMENTATION.getLabelName(), type.name().toLowerCase());
        labels.put(SshImageLabels.CONNECTION_ROLE.getLabelName(), role.toString().toLowerCase());
        List<Image> serverImageList = DOCKER.listImagesCmd().withLabelFilter(labels).withDanglingFilter(false).exec();
        for (Image image : serverImageList) {
            if (image.getLabels() != null) {
                String version = image.getLabels().get(SshImageLabels.VERSION.getLabelName());
                if (version != null) {
                    versionList.add(version);
                }
            }
        }
        return versionList;
    }

    public static List<Image> getAllImages() {
        return DOCKER.listImagesCmd().withLabelFilter(SshImageLabels.IMPLEMENTATION.getLabelName())
            .withDanglingFilter(false).exec();
    }
}
