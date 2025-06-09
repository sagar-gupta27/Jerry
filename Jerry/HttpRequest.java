import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    public String method;
    public String path;
    public String httpVersion;
    public Map<String, String> headers = new HashMap<>();
    public String body;

    @Override
    public String toString() {
        return method + " " + path + " " + httpVersion + "\nHeaders: " + headers + "\nBody:\n" + body;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
