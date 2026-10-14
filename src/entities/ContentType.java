package entities;

public enum ContentType {

    HTML("text/html"),
    CSS("text/css"),
    JS("application/javascript");

    private final String value;

    ContentType(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }


    
}
