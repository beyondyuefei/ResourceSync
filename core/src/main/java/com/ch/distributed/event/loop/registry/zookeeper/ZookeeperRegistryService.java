package com.ch.distributed.event.loop.registry.zookeeper;

import com.ch.distributed.event.loop.common.Node;
import com.ch.distributed.event.loop.component.Component;
import com.ch.distributed.event.loop.lifecycle.LifecycleException;
import com.ch.distributed.event.loop.registry.AbstractRegistryService;
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
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ZookeeperRegistryService extends AbstractRegistryService implements Component {
    private final CuratorFramework zkClient;
    private static final String CLUSTER_PATH = "/resource-sync/cluster";
    private final CuratorCache curatorCache;
    private final CountDownLatch initNodesLatch = new CountDownLatch(1);
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
        addNodeChangeCuratorCacheListener();
    }

    @Override
    public void start() {
        try {
            zkClient.start();
            if (initNodesLatch.await(10, TimeUnit.SECONDS)) {
                LOGGER.info("zookeeper客户端初始化成功");
            } else {
                final String errorMsg = "zookeeper客户端初始化超时!";
                LOGGER.error(errorMsg);
                throw new LifecycleException(errorMsg);
            }
        } catch (Exception e) {
            final String errorMsg = "zookeeper客户端初始化失败";
            LOGGER.error(errorMsg, e);
            try {
                zkClient.close();
            } catch (Exception e1) {
                LOGGER.warn("zookeeper客户端关闭异常", e1);
            }
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
                initNodesLatch.countDown();
                break;
            case RECONNECTED:
                LOGGER.warn("occur reconnect for zookeeper!");
                getLatestNodesAndDoNotify();
                break;
            default:
                LOGGER.warn("occur {} for zookeeper!", connectionState);
        }
    }

    private void addNodeChangeCuratorCacheListener() {
        // fixme: 确认一下 newChildData 与  curatorCache.stream() 返回的ChildData都是一样的吗？即都是最新的
        curatorCache.listenable().addListener((type, oldChildData, newChildData) -> {
            LOGGER.info("监听到节点变化, type: {}", type);
            getLatestNodesAndDoNotify();
        });
    }

    private void getLatestNodesAndDoNotify() {
        final List<Node> nodes = curatorCache.stream()
                // 测试发现，curatorCache 的节点变化回调中会包括监听的父节点，这里要过滤掉
                .filter(childData -> !childData.getPath().equals(CLUSTER_PATH))
                .map(childData -> {
                    final String nodeUniqueKey = childData.getPath().substring(CLUSTER_PATH.length() + 1);
                    return Node.of(nodeUniqueKey);
                })
                .collect(Collectors.toList());

        if (nodes.isEmpty()) {
            LOGGER.info("子节点列表为空，只有父节点 {}，直接返回不处理回调", CLUSTER_PATH);
            return;
        }
        doNotify(nodes);
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
        // 检查节点是否已经存在
        // todo: 为啥断链后ZK的临时znode不会立即删除的情况出现，需确认，比如：是不是因为直接kill进程导致
        try {
            if (zkClient.checkExists().forPath(CLUSTER_PATH + "/" + localNode.getNodeUniqueKey()) != null) {
                LOGGER.warn("节点已存在:{}, 则先删除并重新创建", localNode.getNodeUniqueKey());
                zkClient.delete().forPath(CLUSTER_PATH + "/" + localNode.getNodeUniqueKey());
            }
        } catch (Exception e) {
            LOGGER.error("检查节点是否存在失败:{}", localNode.getNodeUniqueKey(), e);
        }

        try {
            final String currentNodePath = zkClient.create()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(CLUSTER_PATH + "/" + localNode.getNodeUniqueKey(), localNode.getNodeUniqueKey().getBytes());
            LOGGER.info("成功注册当前节点:{}", currentNodePath);
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
