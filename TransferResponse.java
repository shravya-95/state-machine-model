public class TransferResponse extends Response {
    boolean status;
    public TransferResponse(boolean status) {
        this.status = status;
    }

    public boolean getStatus(){
        return this.status;
    }
}
