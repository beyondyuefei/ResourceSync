package com.ch.resource.sync.core.registry;

import java.util.List;

public abstract class AbstractRegistryService implements RegistryService{
    protected List<RegistryNotifyListener> registryNotifyListeners;

    protected void doNotify() {
       // registryNotifyListeners.forEach(RegistryNotifyListener::registryNotify);
    }
}
