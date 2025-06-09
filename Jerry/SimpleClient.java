import java.net.*;
import java.io.*;

public class SimpleClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 8080;

        try (Socket socket = new Socket(hostname, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to server");

            String userInput;
            while ((userInput = console.readLine()) != null) {
                out.println(userInput);
                String response = in.readLine();
                System.out.println("Server: " + response);
                if ("bye".equalsIgnoreCase(userInput.trim())) {
                    break;
                }
            }

        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
        }
    }
}
