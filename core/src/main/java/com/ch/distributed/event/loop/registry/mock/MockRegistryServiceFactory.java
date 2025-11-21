package com.ch.distributed.event.loop.registry.mock;

import com.ch.distributed.event.loop.registry.RegistryService;
import com.ch.distributed.event.loop.registry.RegistryServiceFactory;

import java.util.Map;

public class MockRegistryServiceFactory implements RegistryServiceFactory {
    @Override
    public RegistryService createRegistryService(Map<String, String> configMap) {
        return new MockRegistryService();
    }
}
