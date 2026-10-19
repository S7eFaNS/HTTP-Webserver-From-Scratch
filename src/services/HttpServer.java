package services;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import entities.HttpRequest;
import entities.HttpResponse;
import entities.StatusCodeEnum;
import exceptions.HttpParseException;
import handlers.Router;

public class HttpServer {

    private final int port;
    private final Router router;
    private final ExecutorService threadPool;
    private volatile boolean running;
    private ServerSocket serverSocket;

    public HttpServer(int port, Router router){
        this.port = port;
        this.router = router;
        this.running = true;
        this.threadPool = Executors.newFixedThreadPool(20);
    }

    private static int indexOfHeaderEnd(byte[] data){
        for (int i = 0; i < data.length - 3; i++){

            if(
                data[i] == '\r' &&
                data[i+1] == '\n' &&
                data[i+2] == '\r' &&
                data[i+3] == '\n'
            ){
             return i;   
            }
        }
        return -1;
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

    private static byte[] readBody(InputStream in, byte[] alreadyRead, int parsedContentLength) throws IOException{

        byte[] bodyBytes = new byte[parsedContentLength];

        int copied = Math.min(alreadyRead.length, parsedContentLength);

        System.arraycopy(alreadyRead, 0, bodyBytes, 0, copied);

        int filled = copied;
        
        while(filled < parsedContentLength){
            int n = in.read(bodyBytes, filled, parsedContentLength - filled);
            if(n == -1){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Incomplete body");
            }
            filled += n;
        }

        return bodyBytes;

    }

    private static String readLine(InputStream in) throws IOException{
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int prev = -1;
        while(true){
            int b = in.read();

            if(b == - 1){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Unexpected end of chunked body");
            }

            if (line.size() > 100) {
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Chunk size line too long");
            }

            if(prev == '\r' && b =='\n'){
                byte[] bytes = line.toByteArray();
                return new String(bytes, 0, bytes.length - 1, StandardCharsets.ISO_8859_1);
            }

            line.write(b);
            prev = b;
        }
    }

    private static byte[] readChunkedBody(InputStream in) throws IOException{
        ByteArrayOutputStream body = new ByteArrayOutputStream();

        while(true){

            String line = readLine(in);

            if(line.contains(";")){
                int indexOfMatch = line.indexOf(";");
                line = line.substring(0, indexOfMatch);
            }

            line = line.trim();

            if(!line.matches("[0-9a-fA-F]+")){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Wrong body format");
            }

            if(line.length() > 6){
                throw new HttpParseException(StatusCodeEnum.PAYLOAD_TOO_LARGE, "Chunk is too big");
            }

            int size = Integer.parseInt(line, 16);

            if(size == 0){
                int trailers = 0;
                while(!readLine(in).isEmpty()){
                    if(++trailers > HttpRequestParser.MAX_BODY){
                        throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Too many trailers");
                    }
                }
                break;
            }

            byte[] data = in.readNBytes(size);

            if(data.length < size){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Incomplete chunk");
            }

            if(in.read() != '\r' || in.read() != '\n'){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Chunk size missmatch");
            }

            body.write(data);
            if(body.size() > HttpRequestParser.MAX_BODY){
                throw new HttpParseException(StatusCodeEnum.PAYLOAD_TOO_LARGE, "Body too large");
            }
        
        }
        return body.toByteArray();
    }




    private boolean shouldClose(HttpRequest request){
        String connection = request.getHeaders().get("Connection");

        return "close".equalsIgnoreCase(connection);
    }




    public void start() throws IOException{
        try{
            serverSocket = new ServerSocket(port);
        } catch (IOException e){
            System.out.println("Failed to start a server: " + e.getMessage());
            return;
        }

        while(running){

            try{
                Socket client = serverSocket.accept();

                threadPool.submit(() -> {
                
                    try (client) {
                
                        handleClient(client);
                
                    } catch (IOException e) {
                
                        System.out.println("Connection error: " + e.getMessage());
                
                    }
                });
            } catch(IOException e){
                if(running) {
                    System.out.println(
                        "Accept failed: "
                        + e.getMessage()
                    );
                }
            }
                  
        }
    
    }   

    public void stop(){
        running = false;

        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (Exception e) {
            System.out.println("Error while closing server socket");
        }

        threadPool.shutdown();
        try {
            if(!threadPool.awaitTermination(30, TimeUnit.SECONDS)){
                threadPool.shutdownNow();
            }            
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            
            Thread.currentThread().interrupt();
        }
    }

    private void handleClient(Socket client) throws IOException{
                    
        System.out.println("Client connected!");

        client.setSoTimeout(10000);

        while(true){
            OutputStream out = client.getOutputStream();
            
            try{
                InputStream in = client.getInputStream();

                byte[] buf = new byte[1024];
                ByteArrayOutputStream acc = new ByteArrayOutputStream();
                int indexOfBody = 0;

                while(true){
                    int n = in.read(buf);
                    if(n == -1){
                        break;
                    }

                    acc.write(buf, 0, n);
                    indexOfBody = indexOfHeaderEnd(acc.toByteArray());
                    if(indexOfBody != -1){
                        break;
                    }

                }

                if(indexOfBody == -1){
                    System.out.println("Client closed connection");
                    return;
                }

                
                byte[] raw = acc.toByteArray();
                int lineEnd = indexOfCRLF(raw);

                if(lineEnd == -1){
                    return;
                }
            
                //DEBUGGING FEATURES
                System.out.println("New request");
                System.out.println("Thread: " + Thread.currentThread().getName());

                String requestLine = new String(raw, 0 , lineEnd , StandardCharsets.ISO_8859_1);
                String requestHeader = new String(raw, 0, indexOfBody, StandardCharsets.ISO_8859_1);
                byte[] alreadyRead = Arrays.copyOfRange(raw, indexOfBody + 4, raw.length); 


                HttpRequestParser.validateRequestLine(requestLine);

                Map<String, String> headers = HttpRequestParser.parseHeaders(requestHeader);
                    
                int parsedContentLength = HttpRequestParser.parseContentLength(headers);

                byte[] body;
                if(HttpRequestParser.isChunked(headers)){
                    InputStream bodyIn = new SequenceInputStream(new ByteArrayInputStream(alreadyRead), in);
                    body = readChunkedBody(bodyIn);
                } else{
                    body = readBody(in, alreadyRead, parsedContentLength);
                }

                HttpRequest httpRequest = HttpRequestParser.parse(requestLine, headers, body);

                //DEBUGGING FEATURE
                System.out.println(
                    "Parsed: "
                    + httpRequest.getMethod()
                    + " "
                    + httpRequest.getPath()
                );
                
                boolean close = shouldClose(httpRequest);

                HttpResponse response = router.route(httpRequest);

                if(close){
                    response.setHeader("Connection", "close");
                } else {
                    response.setHeader("Connection", "keep-alive");
                }

                ResponseWriter.write(out, response, httpRequest.getMethod().equals("HEAD"));

                if(close){
                    System.out.println("Connection closed");
                    break;
                }
            } catch (HttpParseException e) {
                ResponseWriter.write(out, HttpResponse.text(e.getStatusCode(), e.getMessage()), false);
            } catch(SocketTimeoutException e){
                System.out.println("Keep-alive timeout");
                break;
            } catch (Exception e){
                    ResponseWriter.write(out,  HttpResponse.text(StatusCodeEnum.INTERNAL_SERVER_ERROR, "Internal Server Error"), false);
            }
        }
    }
    
 
}
