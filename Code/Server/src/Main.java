import library.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
    public static void startListening() {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server:: listening on port " + PORT);

            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("Server:: connection from " + client.getInetAddress().getHostAddress());
                    pool.submit(() -> handleBranchHandshake(client));
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

    /**
     * Per-connection handler:
     *  1) Read "bcode~<BRANCH>"
     *  2) Create Data/<BRANCH> if needed
     *  3) Reply "OK"
     */
    private static void handleBranchHandshake(Socket client) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line = in.readLine();
            String branchCode = parseBranchCode(line);
            System.out.println("Server:: branch code received -> " + branchCode);

            File branchFolder = new File(DATA_ROOT, branchCode);
            if (!branchFolder.exists() && !branchFolder.mkdirs()) {
                throw new IllegalStateException("Failed to create folder: " + branchFolder.getPath());
            }

            out.println("OK"); // signals folder is ready

        } catch (Exception e) {
            System.err.println("Server:: handshake error: " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignore) { }
        }
    }

    private static void ensureDataRoot() {
        File root = new File(DATA_ROOT);
        if (!root.exists() && !root.mkdirs()) {
            System.err.println("Server:: cannot create data root at " + root.getAbsolutePath());
        }
    }

    private static String parseBranchCode(String line) {
        if (line == null) throw new IllegalArgumentException("Missing branch code line");
        final String prefix = "bcode~";
        String trimmed = line.trim();
        if (!trimmed.startsWith(prefix) || trimmed.length() <= prefix.length()) {
            throw new IllegalArgumentException("Invalid branch code message: " + line);
        }
        String branch = trimmed.substring(prefix.length()).trim();
        if (branch.isEmpty()) throw new IllegalArgumentException("Empty branch code");
        return branch;
    }
}