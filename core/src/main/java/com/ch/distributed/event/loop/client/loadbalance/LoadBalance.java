package com.ch.distributed.event.loop.client.loadbalance;

import com.ch.distributed.event.loop.client.ResourceHandler;
import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.common.Node;

import java.util.List;

public interface LoadBalance {
    ResourceHandler select(final ResourceHandlerRequest resourceHandlerRequest, final List<Node> nodes);
}
