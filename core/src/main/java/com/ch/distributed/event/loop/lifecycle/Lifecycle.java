package com.ch.distributed.event.loop.lifecycle;

public interface Lifecycle {
    /**
     * fixme: 确认LifecycleException是否为 未检异常、是否需要通过 throws关键字作为方法签名、是否是 Exception(目前是RuntimeException)、@throws 没有这个tag ?
     *
     * @throws LifecycleException
     */
    void start();

    /**
     * @throws LifecycleException
     */
    void stop();
}
