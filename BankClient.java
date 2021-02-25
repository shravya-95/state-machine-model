import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BankClient extends Thread{
    String host;
    int port;
    int[] uids;
    int iterationCount;
    BankClient(int[] uids, int iterationCount, String host, int port){
        this.host = host;
        this.port = port;
        this.uids = uids;
        this.iterationCount = iterationCount;
    }

    public void run(){
        try {
            for(int i=0;i<iterationCount;i++){
                String logMsg = "";
                String[] content = new String[3];
                Socket socket = new Socket(host, port);
                OutputStream out = socket.getOutputStream();
                ObjectOutputStream outstream = new ObjectOutputStream(out);
                InputStream instream = socket.getInputStream();
                ObjectInputStream oinstream = new ObjectInputStream(instream);
                int rnd1 = new Random().nextInt(uids.length);
                int rnd2 = new Random().nextInt(uids.length);
                if (rnd1==rnd2)
                    continue;
                System.out.printf("From %d To %d\n", uids[rnd1], uids[rnd2]);
                System.out.printf("Balance  before %d, %d\n ", getBalance( uids[rnd1], host, port), getBalance( uids[rnd2], host, port));
                Request transferRequest = new TransferRequest(uids[rnd1], uids[rnd2], 10);
                outstream.writeObject(transferRequest);

                TransferResponse transferResponse = (TransferResponse) oinstream.readObject();


//                System.out.print("transfer status");
//                System.out.println(transferResponse.getStatus());
                content[0]="transfer";
                content[1]= rnd1+", "+rnd2+", 10";
                content[2]= String.valueOf(transferResponse.getStatus());
                logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
                writeToLog("clientLogfile.txt",logMsg);

                System.out.printf("\n Balance  after %d, %d \n", getBalance( uids[rnd1], host, port), getBalance( uids[rnd2], host, port));

            }
        } catch (IOException e){
            e.printStackTrace ();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }


    }
    public static void main (String args[]){
        if ( args.length != 4 ) {
            throw new RuntimeException( "Syntax: java BankClient serverHostname severPortnumber threadCount iterationCount" );
        }
        String serverHostname = args[0];
        int serverPortnumber = Integer.parseInt( args[1] );
        int threadCount = Integer.parseInt( args[2] );
        int iterationCount = Integer.parseInt( args[3] );
        System.out.println ("Connecting to " + serverHostname + ":" + serverPortnumber + "..");
        //TODO: change numAccounts to 100
        int numAccounts = 100;

        //1: sequentially create 100 threads
        int [] uids = createAccounts(numAccounts, serverHostname, serverPortnumber);
        //2: sequentially deposit 100 in each of these accounts
        deposit(uids, 100, numAccounts, serverHostname, serverPortnumber);
        //3: get balance. return value for this should be 10,000
        int balance = getTotalBalance(numAccounts, uids, serverHostname, serverPortnumber);
        System.out.printf("In main balanace: %d \n", balance);

        //5: using join to wait for all the threads
        List<BankClient> clientList = transfer(uids, threadCount, iterationCount, serverHostname, serverPortnumber);
        for(int i = 0; i < clientList.size(); i++)
            try {
                clientList.get(i).join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        //6: get balance. return value should be 10,000
        balance = getTotalBalance(numAccounts, uids, serverHostname, serverPortnumber);
        System.out.printf("In main balanace: %d \n", balance);
    }

    private static int[] createAccounts(int numAccounts, String serverHostname,int serverPortnumber) {
        int[] uids = new int[numAccounts];
        try {
            Socket socket;
            ObjectOutputStream os;
            ObjectInputStream is;
            OutputStream out;
            InputStream in;
            for (int i = 0; i < numAccounts; i++) {
                String logMsg = "";
                String[] content = new String[3];
                socket = new Socket (serverHostname, serverPortnumber);
                in = socket.getInputStream();
                out = socket.getOutputStream();
                os = new ObjectOutputStream(out);
                is = new ObjectInputStream (in);

                Request createRequest = new CreateAccountRequest();
                os.writeObject(createRequest);

                CreateAccountResponse createResponse = (CreateAccountResponse) is.readObject();
                uids[i] = createResponse.getUid();
                content[0]="createAccount";
                content[1]= "";
                content[2]= String.valueOf(uids[i]);
                logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
                writeToLog("clientLogfile.txt",logMsg);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return uids;
    }

    private static void deposit(int[] uids, int amount, int numAccounts, String serverHostname,int serverPortnumber) {
        try {
            Socket socket;
            ObjectOutputStream os;
            ObjectInputStream is;
            OutputStream out;
            InputStream in;
            for (int i = 0; i < numAccounts; i++) {
                String logMsg = "";
                String[] content = new String[3];
                socket = new Socket (serverHostname, serverPortnumber);
                in = socket.getInputStream();
                out = socket.getOutputStream();
                os = new ObjectOutputStream(out);
                is = new ObjectInputStream (in);
                Request depositRequest = new DepositRequest(uids[i],amount);
                os.writeObject(depositRequest);
                DepositResponse depositResponse = (DepositResponse) is.readObject();
                content[0]="deposit";
                content[1]= "UID: "+ uids[i] +", "+"Amount: "+ amount;
                content[2]= String.valueOf(depositResponse.getStatus());
                logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
                writeToLog("clientLogfile.txt",logMsg);
            }
        }catch (IOException e){
            e.printStackTrace ();
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }
    private static List<BankClient> transfer(int[] uids, int threadCount, int iterationCount, String host, int port){
        List<BankClient> clientList = new ArrayList<BankClient>();
        for(int i=0;i<threadCount;i++){
            BankClient bankClient = new BankClient(uids, iterationCount, host, port);
            clientList.add(bankClient);
            bankClient.start();
        }
        return clientList;
    }


    public static synchronized void writeToLog(String fileName, String line){
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

    public static int getTotalBalance(int numAccounts, int[] uids, String serverHostname,int serverPortnumber){
        int total = 0;
        try {
            Socket socket;
            ObjectOutputStream os;
            ObjectInputStream is;
            OutputStream out;
            InputStream in;
            for (int i = 0; i < numAccounts; i++) {
                String logMsg = "";
                String[] content = new String[3];
                socket = new Socket (serverHostname, serverPortnumber);
                in = socket.getInputStream();
                out = socket.getOutputStream();
                os = new ObjectOutputStream(out);
                is = new ObjectInputStream (in);
                Request getBalanceRequest = new GetBalanceRequest(uids[i]);
                os.writeObject(getBalanceRequest);
                GetBalanceResponse getBalanceResponse = (GetBalanceResponse) is.readObject();
                total += getBalanceResponse.getBalance();
                System.out.println(total);
                content[0]="getTotalBalance";
                content[1]= "UID: "+ uids[i];
                content[2]= "AccountBalance: "+ getBalanceResponse.getBalance() +", Total so far:"+total;
                logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
                writeToLog("clientLogfile.txt",logMsg);

            }
        }catch (IOException e){
            e.printStackTrace ();
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        return total;
    }
    //for debugging purposes
    public static int getBalance( int uid, String serverHostname,int serverPortnumber){
        int balance = 0;
        try {
            String logMsg = "";
            String[] content = new String[3];
            Socket socket;
            ObjectOutputStream os;
            ObjectInputStream is;
            OutputStream out;
            InputStream in;
            socket = new Socket (serverHostname, serverPortnumber);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            os = new ObjectOutputStream(out);
            is = new ObjectInputStream (in);
            Request getBalanceRequest = new GetBalanceRequest(uid);
            os.writeObject(getBalanceRequest);
            GetBalanceResponse getBalanceResponse = (GetBalanceResponse) is.readObject();
            balance = getBalanceResponse.getBalance();
            content[0]="getBalance";
            content[1]= "UID: "+ uid;
            content[2]= String.valueOf(getBalanceResponse.getBalance());
            logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
            writeToLog("clientLogfile.txt",logMsg);
        }catch (IOException e){
            e.printStackTrace ();
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        return balance;
    }

}

