import java.rmi.RMISecurityManager;
import java.rmi.Naming;
import java.util.Date;
import java.io.*;
import java.rmi.RemoteException;


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
        List<BankClient> clientList = transfer(uids, threadCount, iterationCount, bankServer);
        for(int i = 0; i < clientList.size(); i++)
            try {
                clientList.get(i).join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        BankClient client = new BankClient();
        client.start(bankServer);
    }
    private static List<BankClient> transfer(int[] uids, int threadCount, int iterationCount, BankServer bankServer){
        List<BankClient> clientList = new ArrayList<BankClient>();
        for(int i=0;i<threadCount;i++){
            BankClient bankClient = new BankClient(uids, iterationCount);
            clientList.add(bankClient);
            bankClient.start(bankServer);
        }
        return clientList;


        int numAccounts = 100;

        //1: sequentially create 100 threads
        int [] uids = createAccounts(numAccounts, bankServer);
//        System.out.println(uids[0]);
//        System.out.println(uids[4]);
        //2: sequentially deposit 100 in each of these accounts
        deposit(uids, 100, numAccounts, bankServer);
        //3: get balance. return value for this should be 10,000
        int balance = getTotalBalance(numAccounts, uids, bankServer);
        System.out.printf("In main balanace: %d \n", balance);

        //5: tansfer
        
        //6: get balance
        balance = getTotalBalance(numAccounts, uids, bankServer);
        System.out.printf("In main balanace: %d \n", balance);
    }
    private static int[] createAccounts(int numAccounts, BankServer bankServer) throws RemoteException {
        int[] uids = new int[numAccounts];
        for (int i = 0; i < numAccounts; i++) {
            String logMsg = "";
            String[] content = new String[3];

            uids[i] = bankServer.createAccount();

            content[0]="createAccount";
            content[1]= "";
            content[2]= String.valueOf(uids[i]);
            logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
            writeToLog("clientLogfile.txt",logMsg);
        }
        return uids;
    }

    private static void deposit(int[] uids, int amount, int numAccounts, BankServer bankServer) throws RemoteException {
        try {
            for (int i = 0; i < numAccounts; i++) {
                String logMsg = "";
                String[] content = new String[3];

                boolean status = bankServer.deposit(uids[i], amount);
                content[0]="deposit";
                content[1]= "UID: "+ uids[i] +", "+"Amount: "+ amount;
                content[2]= String.valueOf(status);
                logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
                writeToLog("clientLogfile.txt",logMsg);
            }
        }catch (IOException e){
            e.printStackTrace ();
        }
    }
//    private static List<BankClient> transfer(int[] uids, int threadCount, int iterationCount, String host, int port){
//        List<BankClient> clientList = new ArrayList<BankClient>();
//        for(int i=0;i<threadCount;i++){
//            BankClient bankClient = new BankClient(uids, iterationCount, host, port);
//            clientList.add(bankClient);
//            bankClient.start();
//        }
//        return clientList;
//    }
//
    public static int getTotalBalance(int numAccounts, int[] uids,  BankServer bankServer) throws RemoteException {
        int total = 0;
        try {
            for (int i = 0; i < numAccounts; i++) {
                String logMsg = "";
                String[] content = new String[3];
                int balance = bankServer.getBalance(uids[i]);
                total += balance;
                System.out.println(total);
                content[0]="getTotalBalance";
                content[1]= "UID: "+ uids[i];
                content[2]= "AccountBalance: "+ balance +", Total so far:"+total;
                logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
                writeToLog("clientLogfile.txt",logMsg);
            }
        }catch (IOException e){
            e.printStackTrace ();
        }
        return total;
    }

    //TODO: below synchronized
    public static void writeToLog(String fileName, String line){
        try {
            File oFile = new File(fileName);
            if (!oFile.exists()) {
                oFile.createNewFile();
            }
            if (oFile.canWrite()) {
                BufferedWriter oWriter = new BufferedWriter(new FileWriter(fileName, true));
                oWriter.write (line);
                oWriter.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }
}