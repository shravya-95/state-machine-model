public class GetBalanceResponse extends Response {
    private int balance;
    public GetBalanceResponse(int balance)
    {
        this.balance = balance;
    }
    public int getBalance(){
        return this.balance;
    }
}
