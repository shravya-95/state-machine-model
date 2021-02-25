public class GetBalanceRequest extends Request {
    private int uid;
    public GetBalanceRequest(int uid)
    {
        super("getBalance");
        this.uid=uid;
    }
    public int getUid(){
        return this.uid;
    }


}
