import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class GroupServerImpl implements GroupServer{
    public static List<String> serverReplicaIds;
    public static List<GroupServer> groupServerStubs;
    public void joinGroup(String serverReplicaId) throws RemoteException {
        serverReplicaIds.add(serverReplicaId);


    }
    public void sendMulticastMessage(String msg) {

    }
    public String receiveMulticastMessage(){

    }
    public static void main (String args[]) throws Exception{
        GroupServerImpl groupServer = new GroupServerImpl();
        try{
            GroupServer groupServerstub = (GroupServer) UnicastRemoteObject.exportObject(groupServer, 0);
            Registry localRegistry = LocateRegistry.getRegistry( 5555);
            localRegistry.bind("GroupServer",groupServerstub);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
