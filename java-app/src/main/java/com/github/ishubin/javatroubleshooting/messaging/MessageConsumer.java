package com.github.ishubin.javatroubleshooting.messaging;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class MessageConsumer implements Runnable{
    private static final Logger log = Logger.getLogger(MessageConsumer.class.getCanonicalName());
    private boolean running = false;
    private final DataQueue dataQueue;
    private final Consumer<Message> consumerCallback;

    public MessageConsumer(DataQueue dataQueue, Consumer<Message> consumerCallback) {
        this.dataQueue = dataQueue;
        this.consumerCallback = consumerCallback;
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
            try {
                consumerCallback.accept(message);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        log.info("Consumer Stopped");
    }
}
