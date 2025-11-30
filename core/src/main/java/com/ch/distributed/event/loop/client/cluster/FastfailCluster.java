package com.ch.distributed.event.loop.client.cluster;

import com.ch.distributed.event.loop.client.ResourceHandler;
import com.ch.distributed.event.loop.client.cluster.support.FastfailClusterResourceHandler;

public class FastfailCluster implements Cluster {
    @Override
    public ResourceHandler join() {
        return new FastfailClusterResourceHandler();
    }
}
