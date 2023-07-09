
public class DLThread {

    private static final String resource1 = "Resource 1";
    private static final String resource2 = "Resource 2";

    public static void startThreads() {
        Thread t1 = new Thread(() -> {
            synchronized (resource1) {
                System.out.println("Locking resource 1 in thread 1");

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (resource2) {
                    System.out.println("Locking resource 2 in thread 1");
                }
            }
        });
        Thread t2 = new Thread(() -> {
            synchronized (resource2) {
                System.out.println("Locking resource 2 in thread 2");

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (resource1) {
                    System.out.println("Locking resource 1 in thread 2");
                }
            }
        });

        t1.start();
        t2.start();
    }
}
