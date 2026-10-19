package framework.entities;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {

    private final StatusCodeEnum status;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpResponse(StatusCodeEnum status, byte[] body){
        this.status = status;
        this.headers = new LinkedHashMap<>();
        this.body = body;
    }

    public void setHeader(String name, String value){
        headers.put(name, value);
    }

    public static HttpResponse text(StatusCodeEnum status, String message){

        return bytes(status, message.getBytes(StandardCharsets.UTF_8), ContentType.TEXT);

    }

    public static HttpResponse bytes(StatusCodeEnum status, byte[] body, ContentType type){

        HttpResponse response = new HttpResponse(status, body);
        response.setHeader("Content-Type", type.getValue());
        return response;
    }

    public StatusCodeEnum getStatus(){
        return status;
    }

    public Map<String, String> getHeaders(){
        return headers;
    }

    public byte[] getBody(){
        return body;
    }
    
}