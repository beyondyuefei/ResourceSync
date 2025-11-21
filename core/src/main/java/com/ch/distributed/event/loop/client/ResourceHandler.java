package com.ch.distributed.event.loop.client;

import java.util.concurrent.CompletableFuture;

public interface ResourceHandler {
    <R> ResourceHandlerResponse<R> handle(final ResourceHandlerRequest resourceHandlerRequest);

    <R> ResourceHandlerResponse<CompletableFuture<R>> handleAsync(final ResourceHandlerRequest resourceHandlerRequest);
}
