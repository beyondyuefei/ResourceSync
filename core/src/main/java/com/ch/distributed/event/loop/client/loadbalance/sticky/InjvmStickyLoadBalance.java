package com.ch.distributed.event.loop.client.loadbalance.sticky;

import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.common.Node;

import java.util.concurrent.ConcurrentHashMap;

public class InjvmStickyLoadBalance extends AbstractStickyLoadBalance {
    private final Node localNode = new Node("127.0.0.1", 1211);
    private final ConcurrentHashMap<String, Node> nodeMap = new ConcurrentHashMap<>();

    @Override
    protected Node find(ResourceHandlerRequest resourceHandlerRequest) {
        return nodeMap.computeIfAbsent(resourceHandlerRequest.getKey(), key -> localNode);
    }
}
