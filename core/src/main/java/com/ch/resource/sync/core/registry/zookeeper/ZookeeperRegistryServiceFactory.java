package com.ch.resource.sync.core.registry.zookeeper;

import com.ch.resource.sync.core.registry.RegistryService;
import com.ch.resource.sync.core.registry.RegistryServiceFactory;

import java.util.Map;

public class ZookeeperRegistryServiceFactory implements RegistryServiceFactory {
    @Override
    public RegistryService createRegistryService(final Map<String, String> configMap) {
        final String connectionTimeoutStr = configMap.get("connectionTimeout");
        final Integer connectionTimeout = (connectionTimeoutStr == null ? Integer.MAX_VALUE : Integer.parseInt(connectionTimeoutStr));
        return new ZookeeperRegistryService(configMap.getOrDefault("zkServers", "localhost:2181"), connectionTimeout);
    }
}
