package com.ch.distributed.event.loop.client.cluster.support;

import com.ch.distributed.event.loop.client.ResourceHandler;
import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.client.loadbalance.LoadBalance;
import com.ch.distributed.event.loop.client.loadbalance.sticky.InjvmLoadBalance;
import com.ch.distributed.event.loop.common.Node;
import com.ch.distributed.event.loop.component.Component;
import com.ch.distributed.event.loop.registry.RegistryService;
import com.ch.distributed.event.loop.registry.zookeeper.ZookeeperRegistryServiceFactory;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

public abstract class AbstractClusterResourceHandler implements ClusterResourceHandler, Component {
    private volatile LoadBalance loadBalance;
    private volatile RegistryService registryService;
    private volatile List<Node> nodes;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractClusterResourceHandler.class);

    @Override
    public void start() {
        // fixme: 这里先用InjvmStickyLoadBalance，后续基于SPI扩展
        loadBalance = new InjvmLoadBalance();
        // fixme: 这里先用ZookeeperRegistryService，后续基于SPI扩展、以及Map参数的填充
        registryService = new ZookeeperRegistryServiceFactory().createRegistryService(Collections.emptyMap());
        registryService.subscribe(nodes -> {
            LOGGER.info("收到节点变化通知，原节点 {}，新节点{}", this.nodes, nodes);
            this.nodes = nodes;
        });
    }

    protected final ResourceHandler doSelect(final ResourceHandlerRequest resourceHandlerRequest) {
        return loadBalance.select(resourceHandlerRequest, nodes);
    }

    @Override
    public void stop() {

    }
}
