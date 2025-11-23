package com.ch.distributed.event.loop.client.loadbalance.hash;

import com.ch.distributed.event.loop.client.ResourceHandler;
import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.client.loadbalance.LoadBalance;
import com.ch.distributed.event.loop.common.Node;

import java.util.List;

public abstract class AbstractHashLoadBalance implements LoadBalance {
    @Override
    public ResourceHandler select(final ResourceHandlerRequest resourceHandlerRequest, final List<Node> nodes) {
        return null;
    }

    protected abstract Node hash(final ResourceHandlerRequest resourceHandlerRequest);
}
