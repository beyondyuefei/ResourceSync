package com.ch.resource.sync.core.registry.zookeeper;

import com.ch.resource.sync.core.common.Node;
import com.ch.resource.sync.core.registry.AbstractRegistryService;
import com.ch.resource.sync.core.registry.RegistryNotifyListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ZookeeperRegistryService extends AbstractRegistryService {
    private final CuratorFramework zkClient;
    private RegistryNotifyListener listener;
    private static final String CLUSTER_PATH = "/resource-sync/cluster";
    private CuratorCache curatorCache;
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
        zkClient.start();
    }

    private void handleConnectionStateChange(CuratorFramework curatorFramework, ConnectionState connectionState) {
        switch (connectionState) {
            case CONNECTED:
                LOGGER.info("connect success for zookeeper!");
                ensureClusterPathWithLock();
                getLatestChildDataAndDoNotify();
                break;
            case RECONNECTED:
                LOGGER.warn("occur reconnect for zookeeper!");
                ensureClusterPathWithLock();
                getLatestChildDataAndDoNotify();
                break;
            default:
                LOGGER.warn("occur {} for zookeeper!", connectionState);
        }
    }

    private void getLatestChildDataAndDoNotify() {
        // fixme: 确认一下 newChildData 与  curatorCache.stream() 返回的ChildData都是一样的吗？即都是最新的
        curatorCache.listenable().addListener((type, oldChildData, newChildData) -> {
            LOGGER.info("监听到节点变化, type: {}", type);
            final List<Node> nodes = new ArrayList<>();
            curatorCache.stream().forEach(childData -> {
                final String nodeUniqueKey = childData.getPath().substring(CLUSTER_PATH.length() + 1);
                nodes.add(Node.of(nodeUniqueKey));
            });
            listener.registryNotify(nodes);
        });
    }

    /**
     * 使用分布式锁确保集群路径创建的原子性
     */
    private void ensureClusterPathWithLock() {
        final InterProcessMutex lock = new InterProcessMutex(zkClient, "/locks/cluster-path-creation");
        try {
            if (lock.acquire(5, TimeUnit.SECONDS)) {
                try {
                    if (zkClient.checkExists().forPath(CLUSTER_PATH) == null) {
                        zkClient.create()
                                .creatingParentsIfNeeded()
                                .forPath(CLUSTER_PATH);
                        LOGGER.info("创建集群路径: " + CLUSTER_PATH);
                    } else {
                        LOGGER.debug("集群路径已存在: " + CLUSTER_PATH);
                    }
                } finally {
                    lock.release();
                }
            } else {
                throw new RuntimeException("获取创建集群路径的锁超时");
            }
        } catch (Exception e) {
            LOGGER.error("确保集群路径存在失败", e);
            throw new RuntimeException("确保集群路径存在失败", e);
        }
    }

    @Override
    public void register() {
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
    public void subscribe(RegistryNotifyListener registryNotifyListener) {
        //todo

        // doNotify();
    }

    @Override
    public void unsubscribe() {

    }

    @Override
    public void unregister() {

    }

    @Override
    public List<Node> getNodes() {
        try {
            return null;
            //return zkClient.getChildren().forPath(CLUSTER_PATH).add();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
