import spark.Request;
import spark.Response;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static spark.Spark.get;
import static spark.Spark.port;

public class App {

    public static void main(String[] args) {
        int listenPort = 4050;
        port(listenPort);
        get("/hello", (req, res) -> "Hello World\n");
        get("/cpu", App::heavyTask);
        get("/arr", App::humongousAllocation);
        get("/dead-lock", App::deadLock);
        get("/gc", App::runGC);
        get("/decompressor", App::decompressor);

        System.out.println("Listening on port " + listenPort);
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
        return "compressed and decompressed " + numMB + " MB " + loop + " times";
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