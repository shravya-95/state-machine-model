import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for BankReplica
 */
public interface BankReplica extends Remote {
    public int receiveRequest(String sourceId, Event request) throws RemoteException;
    public void receiveExecute(Event request) throws RemoteException;
    public void receiveHalt(Event clientReq) throws RemoteException;

}


