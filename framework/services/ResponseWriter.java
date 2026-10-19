package framework.services;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import framework.entities.HttpResponse;
import framework.entities.StatusCodeEnum;

public class ResponseWriter {

    public static void write(OutputStream out, HttpResponse response, boolean isHead) throws IOException{
            
        StatusCodeEnum status = response.getStatus();
        byte[] body = response.getBody();

        StringBuilder head = new StringBuilder();

        head.append("HTTP/1.1 ").append(status.getCode()).append(" ").append(status.getReason()).append("\r\n");
        
        for(Map.Entry<String, String> header : response.getHeaders().entrySet()){

            head.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");

        }

        head.append("Content-Length: ").append(body.length).append("\r\n");
        head.append("\r\n");
        
        out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
        if(!isHead){
            out.write(body);  
        }    
        out.flush();
    }
    
}
