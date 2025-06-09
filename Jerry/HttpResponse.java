import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    public String httpVersion = "HTTP/1.1";
    public int statusCode = 200;
    public String statusMessage = "OK";
    public Map<String, String> headers = new HashMap<>();
    public String body = "";

    public HttpResponse() {
        // Set some default headers
        headers.put("Content-Type", "text/plain; charset=utf-8");
        headers.put("Connection", "close");
    }

    public String toHttpString() {
        StringBuilder responseBuilder = new StringBuilder();

        // Status line
        responseBuilder.append(httpVersion)
                .append(" ")
                .append(statusCode)
                .append(" ")
                .append(statusMessage)
                .append("\r\n");

        // Headers
        if (body != null) {
            headers.put("Content-Length", String.valueOf(body.getBytes().length));
        }

        for (Map.Entry<String, String> header : headers.entrySet()) {
            responseBuilder.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        // Blank line to indicate end of headers
        responseBuilder.append("\r\n");

        // Body
        if (body != null) {
            responseBuilder.append(body);
        }

        return responseBuilder.toString();
    }
}
