package handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import entities.HttpResponse;
import entities.StatusCodeEnum;
import entities.HttpRequest;


public class Router {

    private final Map<String , Map<String, Handler>> routes;

    public Router(){
        this.routes = new HashMap<>();
    }

    public void register(String method, String path, Handler handler){

        Map<String, Handler> methods = routes.get(path);

        if(methods == null){
            methods = new HashMap<>();
            routes.put(path, methods);
        }

        methods.put(method, handler);

    }

    public HttpResponse route(HttpRequest request) throws IOException{

        Map<String, Handler> methods = routes.get(request.getTarget());

        if (methods == null) {
            return HttpResponse.text(StatusCodeEnum.NOT_FOUND, "Not Found");
        }

        Handler handler = methods.get(request.getMethod());

        if(handler == null){
            HttpResponse response = HttpResponse.text(StatusCodeEnum.METHOD_NOT_ALLOWED, "Method Not Allowed");
            response.setHeader("Allow", String.join(", ", methods.keySet()));
            return response;
        }

        return handler.handle(request);

    }
    
}
