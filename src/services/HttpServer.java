package services;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import entities.ContentType;
import entities.HttpRequest;
import entities.HttpResponse;
import entities.StatusCodeEnum;
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
            try (client) {
                handleClient(client);
            } catch (IOException e) {
                System.out.println("Connection error: " + e.getMessage());
            }
        }
    
    }   

    private void handleClient(Socket client) throws IOException{
                    
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
                return;
            }

            
            byte[] raw = acc.toByteArray();
            int lineEnd = indexOfCRLF(raw);

            if(lineEnd == -1){
                return;
            }

            String requestLine = new String(raw, 0 , lineEnd , StandardCharsets.ISO_8859_1);

            OutputStream out = client.getOutputStream();

            try {
                Map<String, String> headers = HttpRequestParser.parseHeaders(raw);
                HttpRequest httpRequest = HttpRequestParser.parse(requestLine, headers);
                
                String target = httpRequest.getTarget();

                String filePath = StaticFileService.resolveFilePath(target);

                if(filePath == null){
                    ResponseWriter.write(out, HttpResponse.text(StatusCodeEnum.NOT_FOUND, "Route not found"), false);
                } else{
                    byte[] content = StaticFileService.loadFile(filePath);
                    ResponseWriter.write(out, HttpResponse.bytes(StatusCodeEnum.OK, content, StaticFileService.resolveContentType(filePath)), false);                }
            } catch (HttpParseException e) {
                ResponseWriter.write(out, HttpResponse.text(e.getStatusCode(), e.getMessage()), false);
            } catch (Exception e){
                ResponseWriter.write(out,  HttpResponse.text(StatusCodeEnum.INTERNAL_SERVER_ERROR, "Internal Server Error"), false);
            }

    }
    
    
}
