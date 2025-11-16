package com.ch.resource.sync.core.registry.zookeeper;

import com.ch.resource.sync.core.common.Node;
import com.ch.resource.sync.core.registry.AbstractRegistryService;
import com.ch.resource.sync.core.registry.RegistryNotifyListener;
import com.ch.resource.sync.core.registry.RegistryService;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;

public class ZookeeperRegistryService extends AbstractRegistryService {
    private final ZkClient zkClient;

    public ZookeeperRegistryService(final String zkServers, final Integer connectionTimeout) {
        if (zkServers == null) {
            throw new IllegalArgumentException("zkServers can not be null");
        }
        this.zkClient = new ZkClient(zkServers, connectionTimeout);
    }

    @Override
    public void register(Node node) {

    }

    @Override
    public void subscribe(RegistryNotifyListener listener) {
        //todo

        doNotify();
    }

    @Override
    public List<Node> getNodes() {
        return List.of();
    }
}
