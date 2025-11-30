package com.ch.distributed.event.loop.client.loadbalance.sticky;

import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.client.loadbalance.AbstractLoadBalance;
import com.ch.distributed.event.loop.common.Node;

import java.util.List;

public class RedisStickLoadBalance extends AbstractLoadBalance {
    @Override
    protected Node find(ResourceHandlerRequest resourceHandlerRequest, final List<Node> nodes) {
        // todo redis
        return null;
    }
}
