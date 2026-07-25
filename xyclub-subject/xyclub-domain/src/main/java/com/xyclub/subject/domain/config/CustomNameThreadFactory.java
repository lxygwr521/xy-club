package com.xyclub.subject.domain.config;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 为业务线程池生成可识别的线程名。
 * 方便排查问题（如日志中看到 order-pool-1-thread-3，就知道是订单服务的线程池创建的。
 */
public class CustomNameThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    CustomNameThreadFactory(String name) {
        SecurityManager securityManager = System.getSecurityManager();
        group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        if (name == null || name.trim().isEmpty()) {
            name = "pool";
        }
        namePrefix = name + "-" + POOL_NUMBER.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
