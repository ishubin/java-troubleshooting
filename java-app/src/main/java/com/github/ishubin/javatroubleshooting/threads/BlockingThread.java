package com.github.ishubin.javatroubleshooting.threads;

import java.util.concurrent.locks.ReentrantLock;

public class BlockingThread extends Thread {
    public BlockingThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        System.out.println("Running thread: " + getName());
        doSomething();
        System.out.println("Thread finished: " + getName());
    }

    synchronized static void doSomething() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
