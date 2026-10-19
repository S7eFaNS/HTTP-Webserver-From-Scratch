import java.io.IOException;
import java.util.Map;

import entities.ContentType;
import entities.HttpResponse;
import entities.StatusCodeEnum;
import handlers.Handler;
import handlers.Router;
import handlers.StaticFileHandler;
import services.HttpServer;

public class Main {

    public static void main(String[] args)  {

        Router router = new Router();

        Handler staticFiles = new StaticFileHandler();

        router.register("GET", "/", staticFiles);

        router.register("GET", "/style.css", staticFiles);

        router.register("GET", "/app.js", staticFiles);

        router.register("GET", "/health", req -> HttpResponse.text(StatusCodeEnum.OK, "up"));

        router.register("GET", "/echo", req -> {
            StringBuilder sb = new StringBuilder();
            sb.append("method: ").append(req.getMethod()).append("\n");
            sb.append("path: ").append(req.getPath()).append("\n");
            sb.append("query: ").append(req.getQuery()).append("\n");
            sb.append("headers:\n");
            for(Map.Entry<String, String> header : req.getHeaders().entrySet()){
                sb.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
            }
            return HttpResponse.text(StatusCodeEnum.OK, sb.toString());
        });

        router.register("POST", "/echo", req -> HttpResponse.bytes(StatusCodeEnum.OK, req.getBody(), ContentType.TEXT));

        HttpServer server = new HttpServer(8080, router);

        Runtime.getRuntime().addShutdownHook(
            new Thread(()-> {
                System.out.println("Server is shutting down");
                server.stop();
            })
        );

        System.out.println("Server is running...");
        try{
            server.start();
        } catch(IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        
    }

}
