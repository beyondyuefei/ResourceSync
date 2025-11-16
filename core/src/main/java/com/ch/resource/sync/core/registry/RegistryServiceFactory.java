package com.ch.resource.sync.core.registry;

import java.util.Map;

public interface RegistryServiceFactory {
    RegistryService createRegistryService(final Map<String, String> configMap);
}
