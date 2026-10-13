import exceptions.HttpParseException;

public class HttpRequest {

    private final String method;
    private final String target;
    private final String version;

    public HttpRequest(String method, String target, String version){
        this.method = method;
        this.target = target;
        this.version = version;
    }

    public static HttpRequest parse(String requestLine){
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

        return new HttpRequest(parts[0], parts[1], parts[2]);    
    }

    public String getMethod(){
        return method;
    }

    public String getTarget(){
        return target;
    }

    public String getVersion(){
        return version;
    }

    @Override 
    public String toString(){
        return method + " " + target + " " + version;
    }
    
}
