package com.ch.resource.sync.core.component;

import com.ch.resource.sync.core.ResourceKey;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public class ResourceExecutorComponentTest {
    // fixme 使用 assertEquals 测试
    @Test
    public void testResourceExecutor() {
        ResourceExecutorComponent resourceExecutorComponent = new ResourceExecutorComponent(10);
        resourceExecutorComponent.start();

        final ResourceKey resourceKey_1 = () -> "key_1";
        final ResourceKey resourceKey_2 = () -> "key_2";

        resourceExecutorComponent.execute(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey_1.getKey()), resourceKey_1);
        resourceExecutorComponent.execute(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey_2.getKey()), resourceKey_2);
        resourceExecutorComponent.execute(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey_1.getKey()), resourceKey_1);
        resourceExecutorComponent.execute(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey_2.getKey()), resourceKey_2);
        resourceExecutorComponent.stop();
    }

    // fixme 使用 assertEquals 测试
    @Test
    public void testResourceExecutorAsync() {
        ResourceExecutorComponent resourceExecutorComponent = new ResourceExecutorComponent(10);
        resourceExecutorComponent.start();

        final ResourceKey resourceKey_1 = () -> "key_1";
        final ResourceKey resourceKey_2 = () -> "key_2";

        final CompletableFuture<Void> completableFuture_1 = resourceExecutorComponent.executeAsync(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey_1.getKey()), resourceKey_1);
        final CompletableFuture<Void> completableFuture_2 = resourceExecutorComponent.executeAsync(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey_2.getKey()), resourceKey_2);
        final CompletableFuture<Void> completableFuture_3 = resourceExecutorComponent.executeAsync(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey_1.getKey()), resourceKey_1);
        final CompletableFuture<Void> completableFuture_4 = resourceExecutorComponent.executeAsync(() -> System.out.println(Thread.currentThread().getName() + ": " + resourceKey_2.getKey()), resourceKey_2);
        CompletableFuture.allOf(completableFuture_1, completableFuture_2, completableFuture_3, completableFuture_4).join();
        resourceExecutorComponent.stop();
    }
}
