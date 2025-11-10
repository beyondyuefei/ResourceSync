package com.ch.resource.sync.core.client;

import com.ch.resource.sync.core.common.ResourceKey;
import com.ch.resource.sync.core.common.ResourceHandlerName;

public interface ResourceHandlerRequest extends ResourceKey {
    <T> T payload();

    ResourceHandlerName resourceHandlerName();
}
