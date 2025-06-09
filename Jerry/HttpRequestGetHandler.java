public class HttpRequestGetHandler implements HttpHandler {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
          HttpResponse httpResponse = new HttpResponse();
           httpResponse.body = "Your Get Request is hanlded";
           httpResponse.statusCode = 200;
           httpResponse.statusMessage = "OK";

           httpResponse.toHttpString();

           return httpResponse;
    }
    
}
