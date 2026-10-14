package services;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import entities.HttpRequest;
import exceptions.HttpParseException;

    public class HttpRequestParser {

        private static final int MAX_HEADERS = 100;

        public static HttpRequest parse(String requestLine, Map<String, String> headers){
        String[] parts = requestLine.split(" ");

            if(parts.length != 3){
                throw new HttpParseException(400, "Expected 3 parts in request line, got: " + parts.length);
            }
            else if(!parts[2].matches("HTTP/\\d+\\.\\d+")){
                throw new HttpParseException(400, "Bad Request");
            }
            else if (!parts[2].equals("HTTP/1.1") && !parts[2].equals("HTTP/1.0")){
                throw new HttpParseException(505, "HTTP Version Not Supported");
            }

        return new HttpRequest(parts[0], parts[1], parts[2], headers);        
    }    

        public static Map<String, String> parseHeaders(byte[] raw){

            String request = new String(raw, StandardCharsets.ISO_8859_1);

            String[] lines = request.split("\r\n");

            Map<String, String> headers = new HashMap<>();

            for(int i = 1; i < lines.length; i++){

                String line = lines[i];
                if(line.isEmpty()){
                    break;
                }

                int colonIndex = line.indexOf(':');
                if(colonIndex == -1){
                    throw new HttpParseException(400, "Malformed header");
                }

                String name = line.substring(0, colonIndex).trim();

                String value = line.substring(colonIndex + 1).trim();

                headers.put(name, value);

            }

            if(headers.size() > MAX_HEADERS){
                throw new HttpParseException(431, "Too many headers");
            }

            return headers;
            
        }
    
}
