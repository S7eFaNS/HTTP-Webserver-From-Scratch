package services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import entities.ContentType;
import entities.StatusCodeEnum;
import exceptions.HttpParseException;

public class StaticFileService {

    private static final Map<String, String> staticRoutes = Map.of(
        "/", "frontend/index.html",
        "/style.css", "frontend/style.css",
        "/app.js", "frontend/app.js"
    );

    public static String resolveFilePath(String target){
        return staticRoutes.get(target);
    }

    public static String loadFile(String path) throws IOException{

        Path filePath = Path.of(path);

        if(!Files.exists(filePath)) {
            throw new HttpParseException(StatusCodeEnum.NOT_FOUND,"Page not found");
        }

        return Files.readString(filePath);

    }

    public static ContentType resolveContentType(String filepath){
        if(filepath.endsWith(".html")){
            return ContentType.HTML;
        } 
        if(filepath.endsWith(".css")){
            return ContentType.CSS;
        }
        if(filepath.endsWith(".js")){
            return ContentType.JS;
        } 
        return ContentType.HTML;
    }

}
