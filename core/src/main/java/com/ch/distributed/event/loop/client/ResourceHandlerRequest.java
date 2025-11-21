package com.ch.distributed.event.loop.client;

import com.ch.distributed.event.loop.common.ResourceKey;
import com.ch.distributed.event.loop.common.ResourceHandlerName;

public interface ResourceHandlerRequest extends ResourceKey {
    <T> T payload();

    ResourceHandlerName resourceHandlerName();
}
