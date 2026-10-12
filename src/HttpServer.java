package src;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private final int port;

    public HttpServer(int port){
        this.port = port;
    }



    public void start() throws IOException{

        ServerSocket serverSocket;

        try{
            serverSocket = new ServerSocket(port);
        } catch (IOException e){
            System.out.println("Failed to start a server: " + e.getMessage());
            return;
        }

            boolean isRunning = true;
        
        while(isRunning){

            Socket client = serverSocket.accept();

            System.out.println("Client connected!");

            OutputStream out = client.getOutputStream();

            String body = "Hello World";
            String response =   
                "HTTP/1.1 200 OK\r\n" +  
                "Content-Type: text/plain\r\n" + 
                "Content-Length: " +  body.length() + "\r\n" + 
                "\r\n" + 
                body;

            out.write(response.getBytes());
            out.flush();

            client.close();


    
    }   

    }
    
}
