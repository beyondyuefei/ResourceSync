package com.ch.distributed.event.loop.client.loadbalance.hash;

import com.ch.distributed.event.loop.client.ResourceHandlerRequest;
import com.ch.distributed.event.loop.client.loadbalance.AbstractLoadBalance;
import com.ch.distributed.event.loop.common.Node;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.TreeMap;

/**
 * todo: 确认hash环需要每一次请求时构建吗？影响性能，是否在构造函数中初始化 ？
 */
public class KetamaConsistentHash extends AbstractLoadBalance {
    // todo: 每个节点的虚拟节点数量，如果要可配置呢 ？
    private static final int NODE_COPY_NUM = 100;
    private final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(KetamaConsistentHash.class);

    @Override
    protected Node find(ResourceHandlerRequest resourceHandlerRequest, final List<Node> nodes) {
        final TreeMap<Long, Node> hashCycle = buildHashCycle(nodes);
        if (hashCycle != null && !hashCycle.isEmpty()) {
            return findNodeByKey(resourceHandlerRequest.getKey(), hashCycle);
        }
        LOGGER.error("没有可用的节点");
        return null;
    }

    /**
     * 这里(poc)只考虑第一次新建哈希环，不考虑后续节点的变化引起的重建
     */
    private TreeMap<Long, Node> buildHashCycle(final List<Node> nodes) {
        final TreeMap<Long, Node> hashCycle = new TreeMap<>();
        for (final Node node : nodes) {
            // 每4个虚拟节点组成一环
            for (int i = 0; i < NODE_COPY_NUM / 4; i++) {
                // 这组虚拟节点唯一的16bit的名称
                final byte[] md5s;
                try {
                    md5s = MessageDigest.getInstance("MD5").digest((node.getIp() + "@" + i).getBytes(StandardCharsets.UTF_8));
                } catch (NoSuchAlgorithmException e) {
                    LOGGER.error("获取md5失败", e);
                    return hashCycle;
                }
                // 在这16bit的md5串中，每4个为一个long，作为虚拟节点的key
                for (int offset = 0; offset < 4; offset++) {
                    long hash = getHash(md5s, offset);
                    // 记录虚拟节点的hash值和节点的关联关系
                    hashCycle.put(hash, node);
                }
            }
        }
        return hashCycle;
    }

    /**
     *
     * todo: lock
     */
    private Node findNodeByKey(final String key, final TreeMap<Long, Node> hashCycle) {
        try {
            final long keyHash = getHash(MessageDigest.getInstance("MD5").digest((key).getBytes(StandardCharsets.UTF_8)), 0);
            if (hashCycle.containsKey(keyHash)) {
                return hashCycle.get(keyHash);
            } else {
                final Long higherKey = hashCycle.higherKey(keyHash);
                return hashCycle.get(higherKey);
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("获取hash值失败", e);
            return null;
        }
    }

    private long getHash(byte[] md5s, int offset) {
        return (((long) (md5s[3 + offset * 4] & 0xFF) << 24)
                | ((long) (md5s[2 + offset * 4] & 0xFF) << 16)
                | ((long) (md5s[1 + offset * 4] & 0xFF) << 8)
                | ((long) md5s[offset * 4] & 0xFF)) & 0xffffffffL;
    }
}
