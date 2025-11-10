package com.ch.resource.sync.core.component;

import com.ch.resource.sync.core.common.ResourceKey;
import com.ch.resource.sync.core.lifecycle.Lifecycle;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ResourceExecutorComponent implements Lifecycle {
    private final EventLoopGroup eventLoopGroup;
    // fixme 机器节点之间出现rebalance后，需要考虑清空缓存 ？因为一些key可能不会再路由过来了
    private final LoadingCache<String, EventLoop> eventLoopLoadingCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(1))
            .build((notUsed) -> next());


    public ResourceExecutorComponent() {
        this(Math.max(1, Runtime.getRuntime().availableProcessors() * 2));
    }

    public ResourceExecutorComponent(final int threadNum) {
        this.eventLoopGroup = new DefaultEventLoopGroup(threadNum);
    }

    public void execute(Runnable runnable, ResourceKey resourceKey) {
        final EventLoop eventLoop = getEventLoop(resourceKey);
        eventLoop.execute(runnable);
    }

    public CompletableFuture<Void> executeAsync(Runnable runnable, ResourceKey resourceKey) {
        final EventLoop eventLoop = getEventLoop(resourceKey);
        return CompletableFuture.runAsync(() -> {
            try {
                // todo 待优化，支持超时、异常封装到CF等
                eventLoop.submit(runnable).sync();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    EventLoop getEventLoop(ResourceKey resourceKey) {
        return eventLoopLoadingCache.get(resourceKey.getKey());
    }

    private EventLoop next() {
        return eventLoopGroup.next();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        eventLoopGroup.close();
        eventLoopLoadingCache.invalidateAll();
    }
}
