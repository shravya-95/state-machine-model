import java.io.Serializable;

//TODO: check if you want to make it abstract
public class Request implements Serializable {

    private String requestType;
    public String getRequestType(){
        return this.requestType;
    }

    public Request(String requestType){
        this.requestType = requestType;
    }
}

