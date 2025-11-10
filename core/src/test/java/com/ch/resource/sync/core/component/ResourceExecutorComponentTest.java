package com.ch.resource.sync.core.component;

import com.ch.resource.sync.core.common.ResourceKey;
import io.netty.channel.EventLoop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public class ResourceExecutorComponentTest {
    @Test
    public void testSameEventLoopPerResourceKey() {
       final ResourceExecutorComponent resourceExecutorComponent = new ResourceExecutorComponent();
       resourceExecutorComponent.start();

        final ResourceKey resourceKey_1 = () -> "key_1";
        final ResourceKey resourceKey_2 = () -> "key_2";

        final EventLoop eventLoop_1_1 =resourceExecutorComponent.getEventLoop(resourceKey_1);
        final EventLoop eventLoop_2_1 =resourceExecutorComponent.getEventLoop(resourceKey_2);
        final EventLoop eventLoop_1_2 =resourceExecutorComponent.getEventLoop(resourceKey_1);
        final EventLoop eventLoop_2_2 =resourceExecutorComponent.getEventLoop(resourceKey_2);
        Assertions.assertEquals(eventLoop_1_1, eventLoop_1_2);
        Assertions.assertEquals(eventLoop_2_1, eventLoop_2_2);
    }

    @Test
    public void testResourceExecutor() {
        ResourceExecutorComponent resourceExecutorComponent = new ResourceExecutorComponent();
        resourceExecutorComponent.start();
        final ResourceKey resourceKey = () -> "key_1";
        resourceExecutorComponent.execute(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey.getKey()), resourceKey);
        resourceExecutorComponent.stop();
    }

    @Test
    public void testResourceExecutorAsync() {
        ResourceExecutorComponent resourceExecutorComponent = new ResourceExecutorComponent(10);
        resourceExecutorComponent.start();
        final ResourceKey resourceKey = () -> "key_1";
        final CompletableFuture<Void> completableFuture = resourceExecutorComponent.executeAsync(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey.getKey()), resourceKey);
        completableFuture.join();
        resourceExecutorComponent.stop();
    }
}
