import java.rmi.RMISecurityManager;
import java.rmi.Naming;
import java.util.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;


public class BankClient extends Thread {
    int[] uids;
    int iterationCount;
    BankServer bankServer;

    /**
     * Constructor for the BankCleint class
     * @param uids List of UIDs for client usage
     * @param iterationCount Number of iterations for transfer
     * @param bankServer bankserver RMI object
     */
    BankClient(int[] uids, int iterationCount, BankServer bankServer){

        this.uids = uids;
        this.iterationCount = iterationCount;
        this.bankServer=bankServer;
    }

    /**
     * Runs when start method is called on BankClient object
     */
    public void run(){

        for (int i=0;i<iterationCount;i++){
            boolean status = false;
            String logMsg = "";
            String[] content = new String[3];
            int rnd1 = new Random().nextInt(uids.length);
            int rnd2 = new Random().nextInt(uids.length);
            if (rnd1==rnd2) {
                continue;
            }
            try {
                status = this.bankServer.transfer(uids[rnd1],uids[rnd2],10);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            //write to log file
            content[0]="transfer";
            content[1]="From:"+ uids[rnd1] +", To:"+ uids[rnd2] +", Amount:"+ 10;
            content[2]= String.valueOf(status);
            logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
            writeToLog("clientLogfile.txt",logMsg);



        }

    }


    public static void main (String args[]) throws Exception {
        if ( args.length != 4 ) {
            throw new RuntimeException( "Syntax: java BankClient serverHostname severPortnumber threadCount iterationCount" );
        }
        System.setSecurityManager (new SecurityManager ());
        BankServer bankServer = (BankServer) Naming.lookup ("//" + args[0] + ":"+ args[1]+"/BankServer");
        int iterationCount = Integer.parseInt(args[2]);
        int threadCount = Integer.parseInt(args[3]);
        int numAccounts = 100;

        //1: sequentially create 100 threads
        int [] uids = createAccounts(numAccounts, bankServer);
        //2: sequentially deposit 100 in each of these accounts
        deposit(uids, 100, numAccounts, bankServer);
        //3: get balance. return value for this should be 10,000
        int balance = getTotalBalance(numAccounts, uids, bankServer);
        System.out.printf("Balance (should be 10,000): %d \n", balance);

        //5: transfer
        List<BankClient> clientList = transfer(uids, threadCount, iterationCount, bankServer);
        for(int i = 0; i < clientList.size(); i++)
            try {
                clientList.get(i).join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        //6: get balance
        balance = getTotalBalance(numAccounts, uids, bankServer);
        System.out.printf("Balance (should be 10,000): %d \n", balance);
    }


    /**
     * Creates mentioned number of accounts
     * @param numAccounts Total number of accounts
     * @param bankServer BankServer RMI object
     * @return List of UIDs of the accounts created
     * @throws RemoteException When communication related exception occurs
     */
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

    /**
     * Deposits mentioned amount in all accounts
     * @param uids List of UIDs
     * @param amount Amount to be deposited in all accounts
     * @param numAccounts total number of accounts
     * @param bankServer BankServer RMI object
     * @throws RemoteException When communication related exception occurs
     */
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

    /**
     * Calculates the totol sum of balance in all accounts after transfers are completed
     * @param numAccounts Total number of accounts
     * @param uids List of all UIDs
     * @param bankServer BankServer RMO Object
     * @return Sum of balance in all accounts
     * @throws RemoteException When communication related exception occurs
     */
    public static int getTotalBalance(int numAccounts, int[] uids,  BankServer bankServer) throws RemoteException {
        int total = 0;
        try {
            for (int i = 0; i < numAccounts; i++) {
                String logMsg = "";
                String[] content = new String[3];
                int balance = bankServer.getBalance(uids[i]);
                total += balance;
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


    /**
     * Writes content to log file. Critical section because multiple threads access the function and only one thread
     * should be able to write to the log file.
     * @param fileName Name of the log file
     * @param line Conent to be written to the file
     */
    public synchronized static void writeToLog(String fileName, String line){
//        synchronized (this){
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
//        }
    }

    /**
     * Creates user mentioned number of client threads to perform transfers and returns them as a list
     * @param uids List of all UIDs
     * @param threadCount Threads transfering concurrenlty, input taken from user
     * @param iterationCount Number of times each thread performs transfer function, input taken from user
     * @param bankServer BankServer RMI Object
     * @return List of threads that were created
     */
    private static List<BankClient> transfer(int[] uids, int threadCount, int iterationCount, BankServer bankServer) {
        List<BankClient> clientList = new ArrayList<BankClient>();
        for (int i = 0; i < threadCount; i++) {
            BankClient bankClient = new BankClient(uids, iterationCount, bankServer);
            clientList.add(bankClient);
            bankClient.start();
        }
        return clientList;
    }
}