package com.ch.resource.sync.core.registry.mock;

import com.ch.resource.sync.core.registry.AbstractRegistryService;
import com.ch.resource.sync.core.registry.RegistryNotifyListener;

public class MockRegistryService extends AbstractRegistryService {
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
