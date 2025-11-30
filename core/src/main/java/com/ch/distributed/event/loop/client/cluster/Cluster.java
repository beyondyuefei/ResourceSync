package com.ch.distributed.event.loop.client.cluster;

import com.ch.distributed.event.loop.client.ResourceHandler;
import com.ch.distributed.event.loop.common.Node;
import com.ch.distributed.event.loop.component.Component;
import com.ch.distributed.event.loop.registry.RegistryNotifyListener;

import java.util.List;

public interface Cluster {
    ResourceHandler join();
}
