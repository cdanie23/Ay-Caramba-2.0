import library.lib;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The entry point to the program
 */
public class Main {
    private static final String DATA_ROOT = "Data";
    private static final int PORT = 5050;
    private static final int THREAD_POOL_SIZE = 4;

    public static void main(String[] args) {
        ensureDataRoot();
        startListening();
    }

    /**
     * Starts the TCP listener and dispatches each client to a worker thread.
     */
    private static void startListening() {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server:: listening on port " + PORT);

            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("Server:: connection from " + client.getInetAddress().getHostAddress());
                    pool.submit(() -> lib.handleClient(client));
                } catch (IOException acceptEx) {
                    System.err.println("Server:: accept error: " + acceptEx.getMessage());
                }
            }
        } catch (IOException bindEx) {
            System.err.println("Server:: startListening error: " + bindEx.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    private static void ensureDataRoot() {
        File root = new File(DATA_ROOT);
        if (!root.exists() && !root.mkdirs()) {
            System.err.println("Server:: cannot create data root at " + root.getAbsolutePath());
        }
    }

}