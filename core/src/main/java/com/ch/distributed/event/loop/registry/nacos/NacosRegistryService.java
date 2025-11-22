package com.ch.distributed.event.loop.registry.nacos;

import com.ch.distributed.event.loop.registry.AbstractRegistryService;
import com.ch.distributed.event.loop.registry.RegistryNotifyListener;

public class NacosRegistryService extends AbstractRegistryService {
    @Override
    public void registerCurrentNode() {
       // do nothing
    }

    @Override
    public void subscribe(RegistryNotifyListener registryNotifyListener) {

    }

    @Override
    public void unsubscribe() {

    }

    @Override
    public void unregisterCurrentNode() {

    }
}
