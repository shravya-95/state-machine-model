import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankServer extends Remote {
  public int createAccount() throws RemoteException;
  public boolean deposit(int uid, int amount) throws RemoteException;
  public int getBalance(int uid) throws RemoteException;
  public boolean transfer(int sourceUid, int targetUid, int amount) throws RemoteException;
  public boolean operate(String clientId, String serverId,int sourceUid, int targetUid, int amount) throws RemoteException;
  public boolean halt() throws RemoteException;
}

