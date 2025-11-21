package com.ch.distributed.event.loop.registry;

import java.util.Map;

public interface RegistryServiceFactory {
    RegistryService createRegistryService(final Map<String, String> configMap);
}
