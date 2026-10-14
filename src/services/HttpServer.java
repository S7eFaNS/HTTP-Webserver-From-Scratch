package services;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import entities.HttpRequest;
import exceptions.HttpParseException;

public class HttpServer {

    private final int port;

    public HttpServer(int port){
        this.port = port;
    }

    private static boolean containsHeaderEnd(byte[] data){
        for (int i = 0; i < data.length - 3; i++){

            if(
                data[i] == '\r' &&
                data[i+1] == '\n' &&
                data[i+2] == '\r' &&
                data[i+3] == '\n'
            ){
             return true;   
            }
        }
        return false;
    }

    private static int indexOfCRLF(byte[] data){
        for(int i = 0; i <data.length - 1;i++){
            if(
                data[i] == '\r' &&
                data[i+1] == '\n'
            ){
                return i;
            }
        }
        return -1;
    }


    private static void sendResponse(OutputStream out, int status, String reason, String body) throws IOException{

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        String headers =
            "HTTP/1.1" + " " + status + " " + reason + "\r\n" +
            "Content-Type: text/plain; charset=utf-8\r\n" +
            "Content-Length: " + bodyBytes.length + "\r\n" +
            "Connection: close\r\n" +
            "\r\n";

            out.write(headers.getBytes(StandardCharsets.ISO_8859_1));
            out.write(bodyBytes);
            out.flush();

    }

    private static String reasonFor(int status){
        return switch(status){
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 505 -> "HTTP Version Not Supported";
            default -> "Unkown";
        };
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

            InputStream in = client.getInputStream();

            byte[] buf = new byte[1024];
            ByteArrayOutputStream acc = new ByteArrayOutputStream();
            boolean complete = false;

            while(true){
                int n = in.read(buf);
                if(n == -1){
                    break;
                }

                acc.write(buf, 0, n);
                if(containsHeaderEnd(acc.toByteArray())){
                    complete = true;
                    break;
                }

            }

            if(!complete){
                client.close();
                continue;
            }

            
            byte[] raw = acc.toByteArray();
            int lineEnd = indexOfCRLF(raw);

            if(lineEnd == -1){
                client.close();
                continue;
            }

            String requestLine = new String(raw, 0 , lineEnd , StandardCharsets.ISO_8859_1);

            OutputStream out = client.getOutputStream();

            try {
                Map<String, String> headers = HttpRequestParser.parseHeaders(raw);
                HttpRequest httpRequest = HttpRequestParser.parse(requestLine, headers);
                System.out.println(httpRequest);
                System.out.println(httpRequest.getHeaders());
                sendResponse(out, 200, reasonFor(200), "This is the path: " + httpRequest.getTarget());
            } catch (HttpParseException e) {
                sendResponse(out, e.getStatusCode(), reasonFor(e.getStatusCode()), e.getMessage());
            }

            client.close();
    
    }   

    }
    
}
