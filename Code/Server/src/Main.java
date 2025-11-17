import library.lib;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The entry point to the program
 */
public class Main {
    private static final String DATA_FOLDER = "Data";
    private static final int NUM_OF_THREADS = 4;
    public static void main(String[] args) {
        File dataFolder = new File(DATA_FOLDER);
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
    }

    private void start_listening(ExecutorService threadPool) {

        try (ServerSocket socket = new ServerSocket(1027)) {
            while (true) {
                Socket client = socket.accept();
                threadPool.execute(() -> {
                    lib.handleClient(client);
                });
            }
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }
}
