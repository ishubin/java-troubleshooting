package com.github.ishubin.javatroubleshooting.messaging;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class MessageConsumer implements Runnable{
    private static final Logger log = Logger.getLogger(MessageConsumer.class.getCanonicalName());
    private boolean running = false;
    private final DataQueue dataQueue;
    private final Consumer<Message> failedMessageConsumer;

    public MessageConsumer(DataQueue dataQueue, Consumer<Message> failedMessageConsumer) {
        this.dataQueue = dataQueue;
        this.failedMessageConsumer = failedMessageConsumer;
    }

    @Override
    public void run() {
        running = true;
        consume();
    }

    public void stop() {
        running = false;
    }

    public void consume() {
        while (running) {

            if (dataQueue.isEmpty()) {
                try {
                    dataQueue.waitIsNotEmpty();
                } catch (InterruptedException e) {
                    log.severe("Error while waiting to Consume messages.");
                    break;
                }
            }

            // avoid spurious wake-up
            if (!running) {
                break;
            }

            Message message = dataQueue.poll();
            useMessage(message);
        }
        log.info("Consumer Stopped");
    }

    private void useMessage(Message message) {
        if ("fail".equals(message.getMessage())) {
            log.info("ERROR: failed to consume message: simulated failure");
            this.failedMessageConsumer.accept(message);
            return;
        }
        if (message != null) {
            //Sleeping on random time to make it realistic
            ThreadUtil.sleep((long) (Math.random() * 100));
            log.info(String.format("[%s] Consuming Message. Topic: %s, Body: %s%n",
                    Thread.currentThread().getName(), message.getTopic(), message.getMessage()));
        }
    }

}
