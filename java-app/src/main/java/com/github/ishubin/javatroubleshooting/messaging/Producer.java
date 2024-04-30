package com.github.ishubin.javatroubleshooting.messaging;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class Producer implements Runnable {
    private static final Logger log = Logger.getLogger(Producer.class.getCanonicalName());
    private static final AtomicInteger idSequence = new AtomicInteger(0);
    private boolean running = false;
    private final DataQueue dataQueue;

    public Producer(DataQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    @Override
    public void run() {
        running = true;
        produce();
    }

    public void stop() {
        running = false;
    }

    public void produce() {

        while (running) {

            if (dataQueue.isFull()) {
                try {
                    dataQueue.waitIsNotFull();
                } catch (InterruptedException e) {
                    log.severe("Error while waiting to Produce messages.");
                    break;
                }
            }

            // avoid spurious wake-up
            if (!running) {
                break;
            }

            dataQueue.add(generateMessage());

//            log.info("Size of the queue is: " + dataQueue.getSize());

            //Sleeping on random time to make it realistic
            ThreadUtil.sleep((long) (Math.random() * 2000 + 100));
        }

        log.info("Producer Stopped");
    }

    private Message generateMessage() {
        int generatedId = (int)(Math.random() * 100000);
        Message message = new Message("default", "Generated message: " + generatedId);
//        log.info(String.format("[%s] Generated Message. Topic: %s, Data: %s%n",
//                Thread.currentThread().getName(), message.getTopic(), message.getMessage()));

        return message;
    }
}
