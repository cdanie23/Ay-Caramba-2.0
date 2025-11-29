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

    public static final String BRANCH_WEEKLY_SALES_TXT = "branch_weekly_sales.txt";
    public static final String LOG_FILE_PATH = "Server.log";
    public static final String SERVER_ABBREVIATION = "Server:: ";

    /**
     * Handles the client connection
     * @param client the socket to the client connection
     */
    public static void handleClient(Socket client) {
        logMsg("Client's IP is -> " + client.getInetAddress().getHostAddress());

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

            String branchCode = in.readLine();
            File branchFolder = createBranchFolder(branchCode);

            out.println("OK");
            logMsg("Sent OK after branch folder creation");

            String payload = in.readLine();
            if (payload == null) {
                throw new IOException("Missing Base64 payload from client");
            }
            logMsg("Encoded payload length -> " + payload.length());

            out.println("OK");

            processData(payload, branchFolder.getPath());

        } catch (Exception e) {
            logMsg("error: " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignore) { }
            logMsg("Connection closed");
        }
    }

    private static File createBranchFolder(String branchCode) {
        if (branchCode == null || !branchCode.startsWith("bcode~") || branchCode.length() <= "bcode~".length()) {
            throw new IllegalArgumentException("Invalid branch code message: " + branchCode);
        }
        String trimmedBranchCode = branchCode.substring("bcode~".length()).trim();
        logMsg("Branch code received -> " + trimmedBranchCode);

        File branchFolder = new File("Data", trimmedBranchCode);
        if (!branchFolder.exists() && !branchFolder.mkdirs()) {
            throw new IllegalStateException("Failed to create folder: " + branchFolder.getPath());
        }
        return branchFolder;
    }

    private static void processData(String base64EncodedString, String branchFolderPath) {
        String trimmedData = base64EncodedString.replaceAll("~", "");
        String data = decodeBase64(trimmedData);
        logMsg("Data decoded -> " + data);
        File weekly_sales = new File(branchFolderPath, BRANCH_WEEKLY_SALES_TXT);
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

    private static synchronized void logMsg(String msg) {
        try (FileWriter fw = new FileWriter(LOG_FILE_PATH, true)) {
            fw.write(SERVER_ABBREVIATION + msg);
            fw.write(System.lineSeparator());
            System.out.println(SERVER_ABBREVIATION + msg);
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }

    }
}
