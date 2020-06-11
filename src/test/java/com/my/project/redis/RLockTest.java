package com.my.project.redis;

import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class RLockTest {

    private RedissonClient redisson;

    @Before
    public void setUp() throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379").setPassword("123456");
        this.redisson = Redisson.create(config);
    }

    @Test
    public void testRLock() throws InterruptedException {
        new Thread(new LockBlock("mylock", 1000 * 60)).start();
        Thread.sleep(1000 * 62);
    }

    @Test
    public void testRLockInMultipleThread() throws InterruptedException {
        Runnable lock1 = new LockBlock("mylock", 1000 * 40);
        Runnable lock2 = new LockBlock("mylock", 1000 * 10);
        new Thread(lock1, "lock1").start();
        new Thread(lock2, "lock2").start();
        Thread.sleep(1000 * 60);
    }

    private class LockBlock implements Runnable {
        private String key;
        private long sleep;
        public LockBlock(String key, long sleep) {
            this.key = key;
            this.sleep = sleep;
        }
        @Override
        public void run() {
            String name = Thread.currentThread().getName();
            RLock lock = redisson.getLock(this.key);

            System.out.println(name + ": get lock...");
            try {
                if(lock.tryLock(30, TimeUnit.SECONDS)) {
                    System.out.println(name + ": get lock success...");
                    try {
                        Thread.sleep(sleep);
                    } finally {
                        System.out.println(name + ": release lock...");
                        lock.unlock();
                        System.out.println(name + ": release lock success...");
                    }
                } else {
                    System.out.println(name + ": get lock failed...");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
