package services;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import entities.HttpMethodEnum;
import entities.HttpRequest;
import entities.StatusCodeEnum;
import exceptions.HttpParseException;

    public class HttpRequestParser {

        private static final int MAX_HEADERS = 100;
        
        
        
        
        private static void validateMethod(String method){
            try{
                HttpMethodEnum.valueOf(method);
            } catch(IllegalArgumentException e){
                throw new HttpParseException(StatusCodeEnum.METHOD_NOT_ALLOWED, "Method not allowed");
            }
        }



        
        public static HttpRequest parse(String requestLine, Map<String, String> headers){
        String[] parts = requestLine.split(" ");

            if(parts.length != 3){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Expected 3 parts in request line, got: " + parts.length);
            }
            else if(!parts[2].matches("HTTP/\\d+\\.\\d+")){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST,"Bad Request");
            }
            else if (!parts[2].equals("HTTP/1.1") && !parts[2].equals("HTTP/1.0")){
                throw new HttpParseException(StatusCodeEnum.HTTP_VERSION_NOT_SUPPORTED,"HTTP Version Not Supported");
            }
            
            validateMethod(parts[0]);

        return new HttpRequest(parts[0], parts[1], parts[2], headers);        
    }    

        
    
    
        public static Map<String, String> parseHeaders(byte[] raw){

            String request = new String(raw, StandardCharsets.ISO_8859_1);

            String[] lines = request.split("\r\n");

            Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

            for(int i = 1; i < lines.length; i++){

                String line = lines[i];
                if(line.isEmpty()){
                    break;
                }

                int colonIndex = line.indexOf(':');
                if(colonIndex == -1){
                    throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Malformed header");
                }

                String name = line.substring(0, colonIndex).trim();

                String value = line.substring(colonIndex + 1).trim();

                headers.put(name, value);

            }

            if(headers.size() > MAX_HEADERS){
                throw new HttpParseException(StatusCodeEnum.REQUEST_HEADER_FIELDS_TOO_LARGE, "Too many headers");
            }

            return headers;
            
        }
    
}
