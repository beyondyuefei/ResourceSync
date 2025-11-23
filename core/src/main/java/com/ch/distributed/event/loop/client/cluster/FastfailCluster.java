package com.ch.distributed.event.loop.client.cluster;

import com.ch.distributed.event.loop.client.ResourceHandler;
import com.ch.distributed.event.loop.common.Node;

import java.util.List;

public class FastfailCluster implements Cluster{
    @Override
    public ResourceHandler join(List<Node> nodes) {
        return null;
    }
}
