package com.ch.distributed.event.loop.registry;

import com.ch.distributed.event.loop.registry.zookeeper.ZookeeperRegistryServiceFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZookeeperRegistryServiceTest {
    private static TestingServer testingServer;
    private static String zkServers;

    @BeforeAll
    public static void init() throws Exception {
        testingServer = new TestingServer(2181);
        testingServer.start();
        zkServers = "127.0.0.1:2181";

        try (CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkServers)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build()) {

            client.start();

            // 等待连接建立
            if (!client.blockUntilConnected(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("无法在10秒内连接到ZooKeeper");
            }
            System.out.println("ZooKeeper 测试服务器已启动并可用");
        }
    }

    @Test
    public void testRegistryAndNotify() {
        final RegistryServiceFactory zookeeperRegistryServiceFactory = new ZookeeperRegistryServiceFactory();
        final Map<String, String> configMap = Map.of("zkServers", zkServers);
        final RegistryService zookeeperRegistryService = zookeeperRegistryServiceFactory.createRegistryService(configMap);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        zookeeperRegistryService.subscribe(nodes -> {
            System.out.println("收到节点变更通知: " + nodes);
            countDownLatch.countDown();
        });
        zookeeperRegistryService.start();
        zookeeperRegistryService.registerCurrentNode();
        try {
            Assertions.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage());
        } finally {
            zookeeperRegistryService.unsubscribe();
        }
    }

    @AfterAll
    public static void close() {
        if (testingServer != null) {
            try {
                testingServer.close();
                System.out.println("ZooKeeper 测试服务器已正常关闭");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
