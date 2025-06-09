import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.Socket;

public class HttpRequestParser {
    public static HttpRequest parse(Socket clientSocket) throws IOException {
       BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        HttpRequest request = new HttpRequest();
        System.out.println("starting the parser");


        // Parse request line
        String requestLine = bufferedReader.readLine();
        if (requestLine == null || requestLine.isEmpty()){
            System.out.println("throuwing");
            throw new IOException("Empty Request");
        }

        System.out.println("parsing request line");
        String[] parts = requestLine.split(" ");
        request.method = parts[0];
        request.path = parts[1];
        request.httpVersion = parts[2];
 
        System.out.println("parsed request line");
        // Parse headers
        String line;
        while (!(line = bufferedReader.readLine()).isEmpty()) {
            String[] header = line.split(":", 2);
            if (header.length == 2) {
                request.headers.put(header[0].trim(), header[1].trim());
            }
        }
        System.out.println("parsed the headers");
        // Parse body if present
        if (request.headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(request.headers.get("Content-Length"));
            char[] bodyChars = new char[contentLength];
            bufferedReader.read(bodyChars, 0, contentLength);
            request.body = new String(bodyChars);
        }
       System.out.println("request parsed successfully");
        return request;
    }
}
