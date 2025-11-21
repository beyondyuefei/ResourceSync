package com.ch.distributed.event.loop.lifecycle;

public class LifecycleException extends RuntimeException{
    public LifecycleException(String message) {
        super(message);
    }

    public LifecycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
