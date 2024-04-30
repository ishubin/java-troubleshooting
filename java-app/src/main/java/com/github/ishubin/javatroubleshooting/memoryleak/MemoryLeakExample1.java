package com.github.ishubin.javatroubleshooting.memoryleak;

import java.util.HashMap;
import java.util.Map;

public class MemoryLeakExample1 {

    public static void run(int numBytes) {
        new LeakThread(new HashMap<>(), numBytes).start();
    }

    public static class LeakThread extends Thread {
        private final Map<BadKey, Object> cache;
        private final int numBytes;

        public LeakThread(Map<BadKey, Object> cache, int numBytes) {
            this.cache = cache;
            this.numBytes = numBytes;
        }

        @Override
        public void run() {
            while(true) {
                byte[] moreBytesToLeak = new byte[numBytes];
                this.cache.put(new BadKey("buffer"), moreBytesToLeak);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class BadKey {
        // no hashCode or equals();
        public final String key;
        public BadKey(String key) {
            this.key = key;
        }
    }
}
