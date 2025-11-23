package com.ch.distributed.event.loop.client;

import java.util.concurrent.CompletableFuture;

public interface ResourceHandler {
    <R> ResourceHandlerResponse<CompletableFuture<R>> handle(final ResourceHandlerRequest resourceHandlerRequest);
}
