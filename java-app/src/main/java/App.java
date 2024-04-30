import com.github.ishubin.javatroubleshooting.DLThread;
import com.github.ishubin.javatroubleshooting.encryption.Encrypt;
import com.github.ishubin.javatroubleshooting.memoryleak.MemoryLeakExample1;
import com.github.ishubin.javatroubleshooting.messaging.*;
import com.github.ishubin.javatroubleshooting.threads.BlockingThread;
import com.github.ishubin.javatroubleshooting.threads.WaitingThread;
import spark.Request;
import spark.Response;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static spark.Spark.*;

public class App {
    private static final Logger consumerLog = Logger.getLogger(MessageConsumer.class.getCanonicalName());

    private static final DataQueue dataQueue = new DataQueue(10000000);
    private static final DataQueue failedDataQueue = new DataQueue(10000000);
    private static final Producer producer = new Producer(dataQueue);
    private final static MessageConsumer failedMessageConsumer = new MessageConsumer(failedDataQueue, dataQueue::add);
    private final static MessageConsumer messageConsumer = new MessageConsumer(dataQueue, message -> {
        if ("fail".equals(message.getMessage())) {
            consumerLog.info("failed message. putting it to another consumer");
            failedDataQueue.add(message);
            return;
        }
        if (message != null) {
            //Sleeping on random time to make it realistic
            ThreadUtil.sleep((long) (Math.random() * 100));
//            consumerLog.info(String.format("[%s] Consuming Message. Topic: %s, Body: %s%n",
//                    Thread.currentThread().getName(), message.getTopic(), message.getMessage()));
        }

    });

    private static final SecureRandom rnd = new SecureRandom();

    private static final List<Object> heapFiller = new ArrayList<>();

    public static void main(String[] args) {
        int listenPort = 4050;
        port(listenPort);
        get("/", (req, res) -> """
        GET /cpu
            runs a CPU heavy task
        
        GET /fill-heap?size=1024&num=100
            fills heap with <num> persistent objects of specified size.
            These objects can only be removed by /clear-heap request
        
        GET /clear-heap
            removes previously created objects from heap
        
        GET /buff?size=1024&num=1
            temporarily generates <num> byte arrays of specified size (<size>) with random bytes.
            It then encrypts them simulating a heavy CPU task
        
        GET /blocking-threads
            creates 100 threads that can only run 1 at a time, while the rest are being blocked by a synchronized method
        
        GET /waiting-threads
            creates 100 threads that can only run 1 at a time, while the rest are waiting to acquire a lock (ReentrantLock)
        
        GET /dead-lock
            starts two threads that end up in a deadlock
        
        GET /gc
            Runs full GC
        
        GET /decompressor?size=1024&loop=1
            Generates random string of <size> bytes and compresses it and decompresses it <loop> number of times
        
        POST /message?message=<message-text>
            Posts a message that would be placed in a queue and picked up by another consumer thread.
            If message text is "fail" then it simulates a failed message that will be placed back on the message queue
            by the consumer, thus simulating a live lock and a high CPU usage between two threads (producer and consumer)
        """);
        get("/cpu", App::heavyTask);
        get("/fill-heap", App::fillHeap);
        get("/clear-heap", App::clearHeap);
        get("/buff", App::processBuffers);
        get("/blocking-threads", App::blockedThreads);
        get("/waiting-threads", App::waitingThreads);
        get("/dead-lock", App::deadLock);
        get("/gc", App::runGC);
        get("/decompressor", App::decompressor);
        get("/memory-leak", App::memoryLeak);
        post("/message", App::postMessage);

        startProducerAndConsumer();

        System.out.println("Listening on port " + listenPort);
    }

    private static String memoryLeak(Request req, Response res) throws Exception {
        int numBytes = Integer.parseInt(req.queryParamOrDefault("size", "1024"));
        MemoryLeakExample1.run(numBytes);
        return "Started a memory leaking thread";
    }

    private static void startProducerAndConsumer() {
        Thread producerThread = new Thread(producer);
        producerThread.start();

        Thread failedConsumerThread = new Thread(failedMessageConsumer);
        failedConsumerThread.start();

        Thread consumerThread = new Thread(messageConsumer) ;
        consumerThread.start();
    }

    private static String blockedThreads(Request req, Response res) throws Exception {
        for (int i = 0; i < 100; i++) {
            new BlockingThread("blocking-thread-" + i).start();
        }
        return "Started blocking threads\n";
    }

    private static String waitingThreads(Request req, Response res) throws Exception {
        for (int i = 0; i < 100; i++) {
            new WaitingThread("waiting-thread-" + i).start();
        }
        return "Started waiting threads\n";
    }

    private static String postMessage(Request req, Response res) throws Exception {
        String message = req.queryParamOrDefault("message", "no-message");
        dataQueue.add(new Message("user", message));
        return "Sent a message\n";
    }


    private static String decompressor(Request req, Response res) throws Exception {
        int numBytes = Integer.parseInt(req.queryParamOrDefault("size", "1024"));
        int loop = Integer.parseInt(req.queryParamOrDefault("loop", "1"));
        for (int i = 0; i < loop; i++) {
            String text = generateRandomString(numBytes);
            byte[] compressedBytes = compressString(text);
            String decompressedText = decompressString(compressedBytes);
        }
        return "compressed and decompressed " + numBytes + " bytes " + loop + " times\n";
    }


    private static String runGC(Request request, Response response) {
        System.gc();
        return "Ran GC\n";
    }

    private static String deadLock(Request request, Response response) {
        DLThread.startThreads();
        return "created dead lock";
    }

    public static Object clearHeap(Request req, Response res) throws Exception {
        heapFiller.clear();
        return "Cleared heap filler\n";
    }
    public static Object fillHeap(Request req, Response res) throws Exception {
        int num = Integer.parseInt(req.queryParamOrDefault("num", "10"));
        int size = Integer.parseInt(req.queryParamOrDefault("size", "1024"));

        for (int i = 0; i < num; i++) {
            byte[] buffer = new byte[size];
            heapFiller.add(buffer);
        }

        return "Generated " + num + " arrays of " + size + " bytes in\n";
    }

    public static Object processBuffers(Request req, Response res) throws Exception {
        long started = System.nanoTime();
        int num = Integer.parseInt(req.queryParamOrDefault("num", "10"));
        int size = Integer.parseInt(req.queryParamOrDefault("size", "1024"));

        for (int i = 0; i < num; i++) {
            byte[] buffer = new byte[size];
            fillBuffer(buffer);
            byte[] encryptedBuffer = encryptBuffer(buffer);
        }

        double elapsed = ((double)(System.nanoTime() - started))/1000000000.0;

        return "Generated " + num + " arrays of " + size + " bytes in " + elapsed + " seconds \n";
    }

    private static void fillBuffer(byte[] buffer) {
        rnd.nextBytes(buffer);
    }

    private static byte[] encryptBuffer(byte[] buffer) throws Exception {
        return Encrypt.encryptData("some-secret-key", buffer);
    }

    public static String heavyTask(Request req, Response res) {
        InputStream resourceStream = App.class.getResourceAsStream("some-test-file.txt");
        if (resourceStream == null) {
            throw new RuntimeException("Cannot find resource");
        }

        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (resourceStream, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return textBuilder.toString();
    }


    public static String generateRandomString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static byte[] compressString(String text) throws UnsupportedEncodingException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Deflater deflater = new Deflater();
        byte[] buffer = new byte[1024];
        byte[] input = text.getBytes(StandardCharsets.UTF_8);

        deflater.setInput(input);
        deflater.finish();
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            os.write(buffer, 0, count);
        }
        return os.toByteArray();
    }

    public static String decompressString(byte[] bytes) throws DataFormatException, UnsupportedEncodingException {
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        byte[] buffer = new byte[1024];

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        while(!inflater.finished()) {
            int len = inflater.inflate(buffer);
            os.write(buffer, 0, len);
        }
        return os.toString(StandardCharsets.UTF_8);
    }
}