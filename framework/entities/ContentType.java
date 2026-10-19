package framework.entities;

public enum ContentType {

    HTML("text/html; charset=utf-8"),
    CSS("text/css; charset=utf-8"),
    JS("application/javascript; charset=utf-8"),
    TEXT("text/plain; charset=utf-8");

    private final String value;

    ContentType(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }


    
}
