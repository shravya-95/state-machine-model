import java.rmi.RMISecurityManager;
import java.rmi.Naming;
import java.util.Date;

public class BankClient extends Thread {
    int[] uids;
    int iterationCount;
    BankClient(int[] uids, int iterationCount){
        this.uids = uids;
        this.iterationCount = iterationCount;
    }
    public void run(BankServer bankServer){
        for (int i=0;i<iterationCount;i++){
            int rnd1 = new Random().nextInt(uids.length);
            int rnd2 = new Random().nextInt(uids.length);
            if (rnd1==rnd2) {
                System.out.println("The accounts picked for transfer were same --- skipping");
                continue;
            }
            boolean status = bankServer.transfer(rnd1,rnd2,10);
            //write to file

        }

    }

    public static void main (String args[]) throws Exception {
        if ( args.length != 2 ) {
            throw new RuntimeException( "Syntax: java BankClient serverHostname severPortnumber" );
        }
        System.setSecurityManager (new SecurityManager ());
        BankServer bankServer = (BankServer) Naming.lookup ("//" + args[0] + ":"+ args[1]+"/BankServer");
        //Same as before here
        bankServer.createAccount();
        int iterationCount
        List<BankClient> clientList = transfer(uids, threadCount, iterationCount, serverHostname, serverPortnumber);
        for(int i = 0; i < clientList.size(); i++)
            try {
                clientList.get(i).join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        BankClient client = new BankClient();
        client.start(bankServer);
    }
    private static List<BankClient> transfer(int[] uids, int threadCount, int iterationCount){
        List<BankClient> clientList = new ArrayList<BankClient>();
        for(int i=0;i<threadCount;i++){
            BankClient bankClient = new BankClient(uids, iterationCount);
            clientList.add(bankClient);
            bankClient.start();
        }
        return clientList;
    }
}