package com.ch.distributed.event.loop.client.loadbalance.sticky;

import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.common.Node;

public class RedisStickLoadBalance extends AbstractStickyLoadBalance{
    @Override
    protected Node find(ResourceHandlerRequest resourceHandlerRequest) {
        return null;
    }
}
