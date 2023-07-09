import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static spark.Spark.get;
import static spark.Spark.port;

public class App {

    public final static Map<String, String> data = new HashMap<>(){{
        put("user.name", "Johny");
    }};
    public static void main(String[] args) {
        port(4050);
        get("/hello", (req, res) -> "Hello World\n");
        get("/heavy-task", App::heavyTask);
        get("/arr", App::humongousAllocation);
        get("/dead-lock", App::deadLock);
        get("/gc", App::runGC);
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

    public static Object heavyTask(Request req, Response res) {
        int num = Integer.parseInt(req.queryParamOrDefault("num", "1000"));
        int size = Integer.parseInt(req.queryParamOrDefault("size", "10"));

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < num; i++) {
            result.append(generateRandomString(size));
        }
        return result.toString();
    }


    public static String generateRandomString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

}