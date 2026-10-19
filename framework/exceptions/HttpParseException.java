package framework.exceptions;

import framework.entities.StatusCodeEnum;

public class HttpParseException extends RuntimeException {

    private final StatusCodeEnum statusCodeEnum;

    public HttpParseException(StatusCodeEnum statusCodeEnum, String message){
        super(message);
        this.statusCodeEnum = statusCodeEnum;
    }

    public StatusCodeEnum getStatusCode(){
        return statusCodeEnum;
    }
    
}
