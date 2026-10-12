import java.io.IOException;

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
