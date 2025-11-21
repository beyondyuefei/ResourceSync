package com.ch.distributed.event.loop.registry.zookeeper;

import com.ch.distributed.event.loop.registry.RegistryService;
import com.ch.distributed.event.loop.registry.RegistryServiceFactory;

import java.util.Map;

public class ZookeeperRegistryServiceFactory implements RegistryServiceFactory {
    @Override
    public RegistryService createRegistryService(final Map<String, String> configMap) {
        final String connectionTimeoutStr = configMap.get("connectionTimeout");
        final Integer connectionTimeout = (connectionTimeoutStr == null ? Integer.MAX_VALUE : Integer.parseInt(connectionTimeoutStr));
        return new ZookeeperRegistryService(configMap.getOrDefault("zkServers", "localhost:2181"), connectionTimeout);
    }
}
