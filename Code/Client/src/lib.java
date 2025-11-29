import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Client utilities
 * Provides Base64 encoding, UTF-8 file reading, and simple append-only logging.
 */
public final class lib {

    private static final String CLIENT_LOG = "client.log";

    private lib() { }

    /**
     * Encodes input as Base64 using UTF-8 bytes.
     *
     * @param input plain text
     * @return Base64-encoded string
     * @throws IllegalArgumentException if input is null
     */
    public static String encode_to_base_64(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reads the entire file as a UTF-8 string, preserving line separators.
     *
     * @param file source file
     * @return file contents as string
     * @throws IOException if I/O fails
     * @throws FileNotFoundException if file is null or does not exist
     */
    public static String readFileUtf8(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new FileNotFoundException(String.valueOf(file));
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (!first) {
                    sb.append(System.lineSeparator());
                }
                sb.append(line);
                first = false;
            }
            return sb.toString();
        }
    }

    /**
     * Writes an INFO line to client.log with timestamp.
     *
     * @param message message to record
     */
    public static void logInfo(String message) {
        appendLog("INFO", message);
    }

    /**
     * Writes an ERROR line to client.log with timestamp.
     *
     * @param message message to record
     */
    public static void logError(String message) {
        appendLog("ERROR", message);
    }

    private static synchronized void appendLog(String level, String message) {
        try (FileWriter fw = new FileWriter(CLIENT_LOG, true)) {
            fw.write(LocalDateTime.now() + " [" + level + "] " + message + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Client log failure: " + e.getMessage());
        }
    }
}
