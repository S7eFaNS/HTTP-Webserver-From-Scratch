package exceptions;

public class HttpParseException extends RuntimeException {

    private final int statusCode;

    public HttpParseException(int statusCode, String message){
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode(){
        return statusCode;
    }
    
}
