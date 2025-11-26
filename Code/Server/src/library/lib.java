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

        try {
            //TODO El's part of the server program goes here
            // NOTE FOR EL: the working directory of this module i.e the server program is
            // Server/.... whatever you want to create a path to
            // that is where the start is for all paths in the server module
            String pathToBranchFolderCreated = null;
            // change the above just a placeholder for me

            //TODO the following needs to be changed
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            String base64EncodedString = reader.readLine();

            System.out.println("Server:: Base64 encoded message -> " + base64EncodedString);
            PrintWriter pout = new PrintWriter(client.getOutputStream(), true);
            pout.println("OK");
            client.close();
            processData(base64EncodedString, pathToBranchFolderCreated);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
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
