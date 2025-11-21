package com.ch.distributed.event.loop.registry;

public interface RegistryService {
    void registerCurrentNode();

    void unregisterCurrentNode();

    // note: 职责上应该是 Cluster需要订阅，并关注集群变化
    void subscribe(final RegistryNotifyListener registryNotifyListener);

    void unsubscribe();
}
