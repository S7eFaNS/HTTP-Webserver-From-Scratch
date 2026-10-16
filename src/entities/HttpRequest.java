package entities;
import java.util.Map;

public class HttpRequest {

    private final String method;
    private final String target;
    private final String path;
    private final String query;
    private final String version;
    private final Map<String, String> headers;

    public HttpRequest(String method, String target, String version, Map<String, String> headers, String path, String query){
        this.method = method;
        this.target = target;
        this.version = version;
        this.headers = headers;
        this.path = path;
        this.query = query;
    }



    public String getMethod(){
        return method;
    }

    public String getTarget(){
        return target;
    }

    public String getPath(){
        return path;
    }

    public String getQuery(){
        return query;
    }

    public String getVersion(){
        return version;
    }

    public Map<String, String> getHeaders(){
        return headers;
    }

    @Override 
    public String toString(){
        return method + " " + target + " " + version;
    }
    
}
