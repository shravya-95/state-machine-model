import java.rmi.*;
import java.util.*;
import java.io.*;
import java.util.List;
import java.util.Random;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class client extends Thread {
    int iterationCount;
    Properties prop;
    String clientId;
    BufferedWriter oWriter;

    /**
     * Constructor for the BankClient class
     * @param iterationCount Number of iterations for transfer
     * @param prop config
     */
    client(Properties prop, String clientId, int iterationCount, BufferedWriter oWriter){

        this.iterationCount = iterationCount;
        this.prop = prop;
        this.clientId = "Client_"+clientId;
        this.oWriter = oWriter;
    }

    /**
     * Runs when start method is called on BankClient object
     */
    public void run() {

//        for (int i=0;i<iterationCount;i++){
                for (int i=0;i<50;i++){


        //can optimize to choose different server if this server is down
            Random rand = new Random();
            //change bound
            int n = rand.nextInt(5);
//            int n = rand.nextInt(2);
            String server = "Server_"+n;
            System.out.println(server);

            Registry registry = null;
            BankServer bankServer;
            try {
                registry = LocateRegistry.getRegistry(prop.getProperty(server+".hostname"), Integer.parseInt(prop.getProperty(server+".rmiregistry")));
            } catch (RemoteException e) {
                throw new RuntimeException("RemoteException: "+e);
            }
            try {
                 bankServer = (BankServer) registry.lookup(server);
            } catch (RemoteException e) {
                throw new RuntimeException("RemoteException: "+e);
            } catch (NotBoundException e) {
                throw new RuntimeException("NotBoundException: "+e);
            }

            boolean status = false;
            String logMsg = "";
            String[] content = new String[3];
            int rnd1 = 1 + new Random().nextInt(19); // random number between 1 and 20
            int rnd2 = 1 + new Random().nextInt(19); // random number between 1 and 20
            if (rnd1==rnd2) {
                continue;
            }
            try {
                status = bankServer.operate(clientId,server, rnd1,rnd2,10);
            } catch (RemoteException e) {
                throw new RuntimeException("RemoteException: "+e);
            }
            //write to log file
            content[0]="transfer";
            content[1]="From:"+ rnd1 +", To:"+ rnd2 +", Amount:"+ 10;
            content[2]= String.valueOf(status);
            logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
            writeToLog(oWriter,logMsg);

        }

    }

    public static Properties loadConfig(String configFileName){
        Properties prop = new Properties();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(configFileName);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Config file not found in path: "+configFileName);
        }
        try {
            prop.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading config file");
        }
        return prop;
    }

    public static void main (String args[]) throws Exception {
        if ( args.length != 3 ) {
            throw new RuntimeException( "Syntax: java client clientId threadCount configFile" );
        }

        String configFileName = args[2];
        Properties prop = loadConfig(configFileName);
        String clientId = "Client_"+args[0];

        BufferedWriter oWriter = startLogging(clientId+".log", clientId);
        int numThreads = Integer.parseInt(args[1]);
        int iterationCount=200;

        List<client> clientList = new ArrayList<client>();
        for (int i = 0; i < numThreads; i++) {
            client bankClient = new client(prop, clientId, iterationCount, oWriter);
            clientList.add(bankClient);
            bankClient.start();
        }

        boolean haltResponse=sendHalt(clientList,prop);


        //write to log file
        writeToLog(oWriter,"halt: "+haltResponse);
        oWriter.close();
    }

    public static boolean sendHalt(List<client> clientList, Properties prop) throws RemoteException {
        //check if all client processes are completed
        for(int i = 0; i < clientList.size(); i++){
            try {
                clientList.get(i).join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        //send HALT to server 0
        Registry registry = null;
        BankServer bankServer;
        try {
            registry = LocateRegistry.getRegistry(prop.getProperty("Server_0.hostname"), Integer.parseInt(prop.getProperty("Server_0.rmiregistry")));
        } catch (RemoteException e) {
            throw new RuntimeException("RemoteException before HALT: "+e);
        }
        try {
            bankServer = (BankServer) registry.lookup("Server_0");
        } catch (RemoteException e) {
            throw new RuntimeException("RemoteException before HALT: "+e);
        } catch (NotBoundException e) {
            throw new RuntimeException("NotBoundException before HALT: "+e);
        }
        return bankServer.halt();
    }
    public static BufferedWriter startLogging(String clientId, String fileName){
        BufferedWriter oWriter = null;
        try {
            File oFile = new File(fileName);
            if (!oFile.exists()) {
                oFile.createNewFile();
                oWriter = new BufferedWriter(new FileWriter(fileName, true));
                oWriter.write("Client ID" + clientId);
            }
            else{
                oWriter = new BufferedWriter(new FileWriter(fileName, true));
                oWriter.write("Client ID" + clientId);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return oWriter;
    }
    /**
     * Writes content to log file. Critical section because multiple threads access the function and only one thread
     * should be able to write to the log file.
     * @param oWriter Name of the log file
     * @param line Conent to be written to the file
     */
    public synchronized static void writeToLog(BufferedWriter oWriter, String line){
//        synchronized (this){
            try {
                    oWriter.write(line);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
    }
}