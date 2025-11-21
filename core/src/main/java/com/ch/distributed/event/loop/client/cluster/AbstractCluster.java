package com.ch.distributed.event.loop.client.cluster;

import com.ch.distributed.event.loop.common.Node;

import java.util.List;

public abstract class AbstractCluster implements Cluster{
    private volatile List<Node> nodes;

    @Override
    public void registryNotify(List<Node> nodes) {

    }

    abstract protected Node select(List<Node> nodes);
}
