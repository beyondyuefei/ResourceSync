package com.ch.resource.sync.core.client;

import java.util.concurrent.CompletableFuture;

public interface ResourceHandler {
    <R> ResourceHandlerResponse<R> handle(final ResourceHandlerRequest resourceHandlerRequest);

    <R> ResourceHandlerResponse<CompletableFuture<R>> handleAsync(final ResourceHandlerRequest resourceHandlerRequest);
}
