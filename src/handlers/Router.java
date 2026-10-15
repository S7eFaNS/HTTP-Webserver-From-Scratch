package handlers;

import java.util.HashMap;
import java.util.Map;

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
    
}
