import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankReplica extends Remote {
//    public int createAccount() throws RemoteException;
//    public boolean deposit(int uid, int amount) throws RemoteException;
//    public int getBalance(int uid) throws RemoteException;
//    public boolean transfer(int sourceUid, int targetUid, int amount) throws RemoteException;
//    public void joinGroup(String serverReplica) throws RemoteException;
//    public void sendMulticastMessage(String msg);
//    public String receiveMulticastMessage();
    public Event receiveRequest(String sourceId, Event request);
}


