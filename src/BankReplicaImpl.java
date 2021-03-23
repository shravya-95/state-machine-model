import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class BankReplicaImpl implements BankReplica {
    public static List<String> serverReplicaIds;
    public static List<BankReplica> bankReplicaStubs;
    public void joinGroup(String serverReplicaId) throws RemoteException {
        serverReplicaIds.add(serverReplicaId);


    }
    public void sendMulticastMessage(String msg) {

    }
    public String receiveMulticastMessage(){

    }
    public static void main (String args[]) throws Exception{
        BankReplicaImpl groupServer = new BankReplicaImpl();
        try{
            BankReplica groupServerstub = (BankReplica) UnicastRemoteObject.exportObject(groupServer, 0);
            Registry localRegistry = LocateRegistry.getRegistry( 5555);
            localRegistry.bind("GroupServer",groupServerstub);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
