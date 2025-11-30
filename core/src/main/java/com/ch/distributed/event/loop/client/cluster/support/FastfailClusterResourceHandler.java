package com.ch.distributed.event.loop.client.cluster.support;

import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.client.ResourceHandlerResponse;
import com.ch.distributed.event.loop.common.ResourceHandlerException;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class FastfailClusterResourceHandler extends AbstractClusterResourceHandler {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FastfailClusterResourceHandler.class);

    @Override
    public <R> ResourceHandlerResponse<CompletableFuture<R>> handle(ResourceHandlerRequest resourceHandlerRequest) {
        try {
            return doSelect(resourceHandlerRequest).handle(resourceHandlerRequest);
        } catch (Exception e) {
            LOGGER.error("处理请求失败，直接抛出异常");
            throw new ResourceHandlerException(e);
        }
    }
}
