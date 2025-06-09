//Maps request to corresponding  handler

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String,HttpHandler> routes = new HashMap<>();

    public void addRoute(String method , String path, HttpHandler handler){
           routes.put(method + " " + path, handler);

    }

    public HttpHandler getHttpHandler(HttpRequest httpRequest){
       return routes.get(httpRequest.getMethod() + " "+ httpRequest.getPath());
    }
}