package framework.services;
import java.util.Map;
import java.util.TreeMap;

import framework.entities.HttpMethodEnum;
import framework.entities.HttpRequest;
import framework.entities.StatusCodeEnum;
import framework.exceptions.HttpParseException;

    public class HttpRequestParser {

        private static final int MAX_HEADERS = 100;
        public static final int MAX_BODY = 1_000_000;
        
        
        
        private static void validateMethod(String method){
            try{
                HttpMethodEnum.valueOf(method);
            } catch(IllegalArgumentException e){
                throw new HttpParseException(StatusCodeEnum.NOT_IMPLEMENTED, "Method not implemented");
            }
        }

        public static void validateRequestLine(String requestLine){
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

        }



        public static boolean isChunked(Map<String, String> headers){
            String te = headers.get("Transfer-Encoding");

            if( te == null){
                return false;
            }
            if(te.equalsIgnoreCase("chunked")){
                return true;
            }
            throw new HttpParseException(StatusCodeEnum.NOT_IMPLEMENTED, "Unsupported Transfer-Encoding");
        }



        
        public static HttpRequest parse(String requestLine, Map<String, String> headers, byte[] body){
        String[] parts = requestLine.split(" ");

            if(!parts[1].startsWith("/")){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Bad request");
            }

            String path;

            String query;

            int queryParam = parts[1].indexOf('?');
            if(queryParam == -1){
                 path = parts[1];
                 query = null;
            } else{
                 path = parts[1].substring(0, queryParam);
                 query = parts[1].substring(queryParam + 1);
            }

        return new HttpRequest(parts[0], parts[1], parts[2], headers, path, query, body);        
    }    

        
    
    
        public static Map<String, String> parseHeaders(String requestHeader){

            String[] lines = requestHeader.split("\r\n");

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

                if(name.equalsIgnoreCase("Content-Length") && headers.containsKey(name) && !headers.get(name).equals(value)){
                    throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Conflicting content-length");
                }

                headers.put(name, value);

            }

            if(headers.size() > MAX_HEADERS){
                throw new HttpParseException(StatusCodeEnum.REQUEST_HEADER_FIELDS_TOO_LARGE, "Too many headers");
            }

            return headers;
            
        }



        public static int parseContentLength(Map<String, String> headers){

            String value = headers.get("Content-Length");

            if(headers.containsKey("Transfer-Encoding") && value != null){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Both Content-Length and Transfer-Encoding present");
            }

            if(value == null){
                return 0;
            }

            if(!value.matches("\\d+")){
                throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Invalid Content-Length");
            }

            long lengthOfContent = Long.parseLong(value);

            if(lengthOfContent > MAX_BODY){
                throw new HttpParseException(StatusCodeEnum.PAYLOAD_TOO_LARGE, "Body too large");
            }

            return (int) lengthOfContent;

        }
    
}
