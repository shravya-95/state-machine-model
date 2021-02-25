public class CreateAccountRequest extends Request {
    private static int uuidCount = 0;
    public CreateAccountRequest() {
        super("createAccount");
    }
    public int getNewUid(){
        return ++uuidCount;
    }
}
