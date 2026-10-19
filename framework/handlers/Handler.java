package framework.handlers;

import java.io.IOException;

import framework.entities.HttpRequest;
import framework.entities.HttpResponse;

@FunctionalInterface 
public interface Handler {

    HttpResponse handle(HttpRequest request) throws IOException;
    
}
