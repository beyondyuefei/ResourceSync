package com.ch.resource.sync.core.lifecycle;

public class LifecycleException extends RuntimeException{
    public LifecycleException(String message) {
        super(message);
    }

    public LifecycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
