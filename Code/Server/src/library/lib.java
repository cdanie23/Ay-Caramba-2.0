package library;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * The library class where all our utility functions reside
 * @author Colby
 */
public class lib {

    /**
     * Handles the client connection
     * @param client the socket to the client connection
     */
    public static void handleClient(Socket client) {
        System.out.println("Server:: Client's IP is -> " + client.getInetAddress().getHostAddress());

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

            String line1 = in.readLine();
            if (line1 == null || !line1.startsWith("bcode~") || line1.length() <= "bcode~".length()) {
                throw new IllegalArgumentException("Invalid branch code message: " + line1);
            }
            String branchCode = line1.substring("bcode~".length()).trim();
            System.out.println("Server:: Branch code received -> " + branchCode);

            File branchFolder = new File("Data", branchCode);
            if (!branchFolder.exists() && !branchFolder.mkdirs()) {
                throw new IllegalStateException("Failed to create folder: " + branchFolder.getPath());
            }

            out.println("OK");
            System.out.println("Server:: Sent OK after branch folder creation");

            String payload = in.readLine();
            if (payload == null) {
                throw new IOException("Missing Base64 payload from client");
            }
            System.out.println("Server:: Encoded payload length -> " + payload.length());

            out.println("OK");

            processData(payload, branchFolder.getPath());

        } catch (Exception e) {
            System.err.println("Server:: error: " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignore) { }
            System.out.println("Server:: Connection closed");
        }
    }

    private static void processData(String base64EncodedString, String branchFolderPath) {
        String trimmedData = base64EncodedString.replaceAll("~", "");
        String data = decodeBase64(trimmedData);
        System.out.println("Server:: Data received -> " + data);
        File weekly_sales = new File(branchFolderPath + "/branch_weekly_sales.txt");
        try (FileWriter fw = new FileWriter(weekly_sales, false)) {
            fw.write(data);
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    private static String decodeBase64(String base64EncodedString) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decodedBytes = decoder.decode(base64EncodedString);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
