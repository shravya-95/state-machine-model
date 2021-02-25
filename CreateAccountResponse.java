public class CreateAccountResponse extends Response  {
    private int uid;
    public CreateAccountResponse(int uid) {
        this.uid = uid;
    }
    public int getUid(){
        return this.uid;
    }
}
