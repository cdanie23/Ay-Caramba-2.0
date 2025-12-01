
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Client entry point
 */
public class Main {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 5050;

    /**
     * Launches the client and initiates the data transfer for a single branch file.
     */
    public static void main(String[] args) {
        System.out.println("Please enter the branch name" + System.lineSeparator());

        Scanner scanner = new Scanner(System.in);
        String branchName = scanner.nextLine();
        scanner.close();
        File inputFile = new File("Data/" + branchName + "/branch_weekly_sales.txt");
        start_data_transfer(inputFile, SERVER_HOST, SERVER_PORT);
    }

    /**
     * Executes the client protocol for one input file.
     *
     * @param inputFile branch_weekly_sales.txt to transmit
     * @param host      server host
     * @param port      server port
     * @throws IllegalArgumentException if inputFile is null or missing
     */
    public static void start_data_transfer(File inputFile, String host, int port) {
        try {
            String fileContent = lib.readFileUtf8(requireFile(inputFile));
            String branchCode = extractBranchCode(fileContent);

            lib.logInfo("client connected");
            lib.logInfo("Sending " + branchCode);

            try (Socket socket = new Socket(host, port);
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                System.out.println("Client:: connected to server");

                out.println("bcode~" + branchCode);
                String ack1 = in.readLine();

                String base64 = lib.encode_to_base_64(fileContent);
                String wrapped = "~" + base64 + "~";

                if (!"OK".equals(ack1)) {
                    System.out.println("Client:: unexpected response (phase 1): " + ack1);
                }

                out.println(wrapped);
                String ack2 = in.readLine();

                if (!"OK".equals(ack2)) {
                    System.out.println("Client:: unexpected response (phase 2): " + ack2);
                }

                System.out.println("File was transferred successfully");
            } finally {
                lib.logInfo("client closed connection");
            }
        } catch (Exception e) {
            System.err.println("Client:: error: " + e.getMessage());
            lib.logError("client error: " + e.getMessage());
        }
    }

    /**
     * Returns the first CSV token (branch code) from the first line of content.
     *
     * @param content file content
     * @return branch code token
     * @throws IllegalStateException if content is malformed
     */
    private static String extractBranchCode(String content) {
        String firstLine = content.split("\\R", 2)[0];
        String[] tokens = firstLine.split(",");
        if (tokens.length < 1) {
            throw new IllegalStateException("Cannot extract branch code");
        }
        return tokens[0].trim();
    }

    /**
     * Validates the file reference.
     *
     * @param file candidate file
     * @return file if valid
     * @throws IllegalArgumentException if null or non-existent
     */
    private static File requireFile(File file) {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("Input file is missing: " + file);
        }
        return file;
    }
}
