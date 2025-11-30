package com.ch.distributed.event.loop.client.remote;

import com.ch.distributed.event.loop.common.Node;

public abstract class AbstractRemoteService implements RemoteService {
    protected final Node node;

    public AbstractRemoteService(Node node) {
        this.node = node;
    }
}
