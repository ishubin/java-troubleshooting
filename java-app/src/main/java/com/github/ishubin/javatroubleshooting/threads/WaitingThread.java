package com.github.ishubin.javatroubleshooting.threads;

import java.util.concurrent.locks.ReentrantLock;

public class WaitingThread extends Thread {
    private static final ReentrantLock lock = new ReentrantLock();

    public WaitingThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        System.out.println("Running thread: " + getName());
        lock.lock();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        System.out.println("Thread finished: " + getName());
    }
}
