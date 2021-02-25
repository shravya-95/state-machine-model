public class DepositRequest extends Request {
    private int uid;
    private int amount;
    public DepositRequest(int uid, int amount) {
        super("deposit");
        this.uid = uid;
        this.amount=amount;
    }
    public int getUid(){
        return this.uid;
    }
    public int getAmount(){
        return this.amount;
    }
}
