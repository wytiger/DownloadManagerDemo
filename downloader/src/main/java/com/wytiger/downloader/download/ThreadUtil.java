package com.wytiger.downloader.download;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 统一处理短时间的任务，减少零散的线程或线程池
 * Created by yutang on 2017/9/8.
 */

public final class ThreadUtil {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static final int KEEP_ALIVE = 1;

    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;

    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private static volatile Handler sMainHandler;

    private static ExecutorService sThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("ThreadUtil"));


    public static Handler initMainHandler() {
        if (null == sMainHandler) {
            synchronized (ThreadUtil.class) {
                if (null == sMainHandler) {
                    sMainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }

        return sMainHandler;
    }


    public static boolean isMainThread() {
        return (Looper.myLooper() == Looper.getMainLooper());
    }


    public static void runOnWorkThread(Runnable runnable) {
        if (runnable != null) {
            sThreadPoolExecutor.execute(runnable);
        }
    }


    public static void runOnUiThread(Runnable runnable) {
        if (runnable != null) {
            if (!isMainThread()) {
                initMainHandler();
                sMainHandler.post(runnable);
            } else {
                runnable.run();
            }
        }
    }

    public static void runOnUiThreadDelayed(Runnable runnable, long delayMillis) {
        if (runnable != null) {
            initMainHandler();
            sMainHandler.postDelayed(runnable, delayMillis);
        }
    }


    public static final class DefaultThreadFactory implements ThreadFactory {

        private String threadPoolName;

        private final AtomicInteger mCounter = new AtomicInteger(1);

        public DefaultThreadFactory(String threadPoolName) {
            this.threadPoolName = threadPoolName;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, threadPoolName + " #" + mCounter.getAndIncrement());
        }
    }
}
