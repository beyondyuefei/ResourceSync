package com.ch.distributed.event.loop.client;

public interface ResourceHandlerResponse<R> {
    boolean isSuccess();

    R data();

    Exception exception();
}
