package src;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

public static void main(String[] args)  {

    System.out.println("Server is running...");

    HttpServer server = new HttpServer(8080);
    try{
        server.start();
    } catch(IOException e){
        System.out.println("Error: " + e.getMessage());
    }
    
    
    }

}
