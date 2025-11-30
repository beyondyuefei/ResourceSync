package com.ch.distributed.event.loop.client.loadbalance.sticky;

import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.client.loadbalance.AbstractLoadBalance;
import com.ch.distributed.event.loop.common.Node;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InjvmLoadBalance extends AbstractLoadBalance {
    private final Node localNode = new Node("127.0.0.1", 1211);
    private final ConcurrentHashMap<String, Node> nodeMap = new ConcurrentHashMap<>();

    @Override
    protected Node find(ResourceHandlerRequest resourceHandlerRequest, final List<Node> nodes) {
        return nodeMap.computeIfAbsent(resourceHandlerRequest.getKey(), key -> localNode);
    }
}
