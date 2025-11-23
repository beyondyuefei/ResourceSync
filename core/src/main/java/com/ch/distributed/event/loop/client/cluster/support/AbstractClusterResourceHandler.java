package com.ch.distributed.event.loop.client.cluster.support;

import com.ch.distributed.event.loop.client.ResourceHandler;
import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.client.loadbalance.LoadBalance;
import com.ch.distributed.event.loop.common.Node;
import com.ch.distributed.event.loop.component.Component;

import java.util.List;

public abstract class AbstractClusterResourceHandler implements ClusterResourceHandler, Component {
    private volatile LoadBalance loadBalance;

    @Override
    public void start() {
        loadBalance = null;
    }

    protected final ResourceHandler doSelect(final ResourceHandlerRequest resourceHandlerRequest, final List<Node> nodes) {
        return loadBalance.select(resourceHandlerRequest, nodes);
    }

    @Override
    public void stop() {

    }
}
