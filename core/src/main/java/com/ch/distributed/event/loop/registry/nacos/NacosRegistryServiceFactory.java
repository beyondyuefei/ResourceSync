package com.ch.distributed.event.loop.registry.nacos;

import com.ch.distributed.event.loop.registry.RegistryService;
import com.ch.distributed.event.loop.registry.RegistryServiceFactory;

import java.util.Map;

public class NacosRegistryServiceFactory implements RegistryServiceFactory {
    @Override
    public RegistryService createRegistryService(Map<String, String> configMap) {
        return new NacosRegistryService();
    }
}
