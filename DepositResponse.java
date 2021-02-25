public class DepositResponse extends Response {
    private boolean status;
    //TODO: string or boolean?
    public DepositResponse(boolean status) {
        this.status = status;
    }
    public boolean getStatus(){
        return this.status;
    }
}
