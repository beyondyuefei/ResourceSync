package com.ch.resource.sync.core.registry.zookeeper;

import com.ch.resource.sync.core.common.Node;
import com.ch.resource.sync.core.component.Component;
import com.ch.resource.sync.core.lifecycle.LifecycleException;
import com.ch.resource.sync.core.registry.AbstractRegistryService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ZookeeperRegistryService extends AbstractRegistryService implements Component {
    private final CuratorFramework zkClient;
    private static final String CLUSTER_PATH = "/resource-sync/cluster";
    private final CuratorCache curatorCache;
    private final CompletableFuture<List<Node>> initNodesFuture = new CompletableFuture<>();
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ZookeeperRegistryService.class);

    public ZookeeperRegistryService(final String zkServers, final Integer connectionTimeout) {
        if (zkServers == null) {
            throw new IllegalArgumentException("zkServers can not be null");
        }

        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(zkServers)
                .sessionTimeoutMs(60000)
                .connectionTimeoutMs(connectionTimeout)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        zkClient.getConnectionStateListenable().addListener(this::handleConnectionStateChange);
        curatorCache = CuratorCache.builder(zkClient, CLUSTER_PATH).build();
    }

    @Override
    public void start() {
        try {
            zkClient.start();
            final List<Node> nodes = initNodesFuture.get(10, TimeUnit.SECONDS);
            if (nodes != null && !nodes.isEmpty()) {
                LOGGER.info("zookeeper客户端初始化成功，zookeeper集群节点: {}", nodes);
            }
        } catch (Exception e) {
            final String errorMsg = "zookeeper客户端初始化失败";
            LOGGER.error(errorMsg, e);
            throw new LifecycleException(errorMsg, e);
        }
    }

    @Override
    public void stop() {
        curatorCache.close();
        zkClient.close();
    }

    private void handleConnectionStateChange(CuratorFramework curatorFramework, ConnectionState connectionState) {
        switch (connectionState) {
            case CONNECTED:
                LOGGER.info("connect success for zookeeper!");
                ensureClusterPathWithLock();
                curatorCache.start();
                addNodeChangeListener();
                break;
            case RECONNECTED:
                LOGGER.warn("occur reconnect for zookeeper!");
                getLatestNodesAndDoNotify();
                break;
            default:
                LOGGER.warn("occur {} for zookeeper!", connectionState);
        }
    }

    private void addNodeChangeListener() {
        // fixme: 确认一下 newChildData 与  curatorCache.stream() 返回的ChildData都是一样的吗？即都是最新的
        curatorCache.listenable().addListener((type, oldChildData, newChildData) -> {
            LOGGER.info("监听到节点变化, type: {}", type);
            initNodesFuture.complete(getLatestNodesAndDoNotify());
        });
    }

    private List<Node> getLatestNodesAndDoNotify() {
        final List<Node> nodes = new ArrayList<>();
        curatorCache.stream().forEach(childData -> {
            final String nodeUniqueKey = childData.getPath().substring(CLUSTER_PATH.length() + 1);
            nodes.add(Node.of(nodeUniqueKey));
        });
        doNotify(nodes);
        return nodes;
    }

    /**
     * 使用分布式锁确保集群路径创建的原子性
     */
    private void ensureClusterPathWithLock() {
        final String nodePath = getCurrentNodePath();
        try {
            if (zkClient.checkExists().forPath(nodePath) == null) {
                try {
                    zkClient.create()
                            .creatingParentsIfNeeded()
                            .forPath(nodePath);
                    LOGGER.info("成功创建znode路径: {}", nodePath);
                } catch (KeeperException.NodeExistsException e) {
                    LOGGER.warn("znode路径重复创建，请确认是否存在重复ip:port的配置: {}", nodePath);
                }
            } else {
                LOGGER.debug("znode路径已存在: {}", nodePath);
            }
        } catch (Exception e) {
            LOGGER.error("创建znode路径失败: {}", nodePath, e);
        }
    }

    @Override
    public void registerCurrentNode() {
        if (zkClient.getState() != CuratorFrameworkState.STARTED) {
            throw new IllegalStateException("zkClient is not started");
        }
        final Node localNode = localNode();
        try {
            zkClient.create()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(CLUSTER_PATH + "/" + localNode.getNodeUniqueKey(), localNode.getNodeUniqueKey().getBytes());
        } catch (Exception e) {
            LOGGER.error("注册节点失败:{}", localNode.getNodeUniqueKey(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregisterCurrentNode() {
        try {
            zkClient.delete().forPath(getCurrentNodePath());
        } catch (Exception e) {
            LOGGER.error("注销节点失败:{}", localNode().getNodeUniqueKey(), e);
            throw new RuntimeException(e);
        }
    }

    private String getCurrentNodePath() {
        return CLUSTER_PATH + "/" + localNode().getNodeUniqueKey();
    }
}
