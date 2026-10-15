package handlers;

import java.io.IOException;

import entities.HttpRequest;
import entities.HttpResponse;

@FunctionalInterface 
public interface Handler {

    HttpResponse handle(HttpRequest request) throws IOException;
    
}
