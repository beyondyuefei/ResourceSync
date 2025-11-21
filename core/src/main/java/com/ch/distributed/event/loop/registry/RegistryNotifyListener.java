package com.ch.distributed.event.loop.registry;

import com.ch.distributed.event.loop.common.Node;

import java.util.List;

public interface RegistryNotifyListener {
    void registryNotify(final List<Node> nodes);
}
