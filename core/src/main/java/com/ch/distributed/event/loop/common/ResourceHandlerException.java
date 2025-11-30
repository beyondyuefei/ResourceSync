package com.ch.distributed.event.loop.common;

public class ResourceHandlerException extends RuntimeException {
    public ResourceHandlerException(String message) {
        super(message);
    }

    public ResourceHandlerException(Throwable cause) {
        super(cause);
    }

    public ResourceHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
