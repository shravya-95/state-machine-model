public class TransferRequest extends Request {
    private int sourceUid, targetUid;
    private int amount;
    public TransferRequest(int sourceUid, int targetUid, int amount) {
        super("transfer");
        this.sourceUid = sourceUid;
        this.targetUid = targetUid;
        this.amount = amount;
    }

    public int getSourceUid() {
        return this.sourceUid;
    }
    public int getTargetUid() {
        return this.targetUid;
    }
    public int getAmount() {
        return this.amount;
    }
}
