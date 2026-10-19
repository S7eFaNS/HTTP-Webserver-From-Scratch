package framework.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import framework.entities.HttpRequest;
import framework.entities.HttpResponse;
import framework.entities.StatusCodeEnum;


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

        Map<String, Handler> methods = routes.get(request.getPath());

        if (methods == null) {
            return HttpResponse.text(StatusCodeEnum.NOT_FOUND, "Not Found");
        }

        Handler handler = methods.get(request.getMethod());

        if(handler == null && request.getMethod().equals("HEAD")){
            handler = methods.get("GET");
        }

        if(handler == null){
            HttpResponse response = HttpResponse.text(StatusCodeEnum.METHOD_NOT_ALLOWED, "Method Not Allowed");
            Set<String> allowed = new LinkedHashSet<>(methods.keySet());
            if(allowed.contains("GET")) {
                allowed.add("HEAD");
            }
            response.setHeader("Allow", String.join(", ", allowed));
            return response;
        }

        return handler.handle(request);

    }
    
}
