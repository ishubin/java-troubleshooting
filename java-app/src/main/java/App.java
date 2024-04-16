import com.github.ishubin.javatroubleshooting.DLThread;
import com.github.ishubin.javatroubleshooting.messaging.MessageConsumer;
import com.github.ishubin.javatroubleshooting.messaging.DataQueue;
import com.github.ishubin.javatroubleshooting.messaging.Message;
import com.github.ishubin.javatroubleshooting.messaging.Producer;
import com.github.ishubin.javatroubleshooting.threads.BlockingThread;
import com.github.ishubin.javatroubleshooting.threads.WaitingThread;
import spark.Request;
import spark.Response;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static spark.Spark.*;

public class App {

    private static DataQueue dataQueue = new DataQueue(10000000);
    private static Producer producer = new Producer(dataQueue);
    private static MessageConsumer messageConsumer = new MessageConsumer(dataQueue, (message -> dataQueue.add(message)));

    public static void main(String[] args) {
        int listenPort = 4050;
        port(listenPort);
        get("/hello", (req, res) -> "Hello World\n");
        get("/cpu", App::heavyTask);
        get("/arr", App::humongousAllocation);
        get("/blocking-threads", App::blockedThreads);
        get("/waiting-threads", App::waitingThreads);
        get("/dead-lock", App::deadLock);
        get("/gc", App::runGC);
        get("/decompressor", App::decompressor);
        post("/message", App::postMessage);

        startProducerAndConsumer();

        System.out.println("Listening on port " + listenPort);
    }

    private static void startProducerAndConsumer() {
        Thread producerThread = new Thread(producer);
        producerThread.start();

        Thread consumerThread = new Thread(messageConsumer);
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
        int numMB = Integer.parseInt(req.queryParamOrDefault("size", "1"));
        int loop = Integer.parseInt(req.queryParamOrDefault("loop", "1"));
        int size = numMB * 1000_000;
        for (int i = 0; i < loop; i++) {
            String text = generateRandomString(size);
            byte[] compressedBytes = compressString(text);
            String decompressedText = decompressString(compressedBytes);
        }
        return "compressed and decompressed " + numMB + " MB " + loop + " times\n";
    }


    private static String runGC(Request request, Response response) {
        System.gc();
        return "Ran GC\n";
    }

    private static String deadLock(Request request, Response response) {
        DLThread.startThreads();
        return "created dead lock";
    }

    public static Object humongousAllocation(Request req, Response res) {
        int num = Integer.parseInt(req.queryParamOrDefault("num", "10"));
        int size = Integer.parseInt(req.queryParamOrDefault("size", "1024"));

        for (int i = 0; i < num; i++) {
            byte[] arr = new byte[size];
        }

        return "Generated " + num + " arrays of " + size + " bytes\n";
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