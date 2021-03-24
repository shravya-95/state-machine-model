import java.rmi.*;
import java.time.LocalDateTime;
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
    int numServers;

    /**
     * Constructor for the BankClient class
     * @param prop config
     * @param clientId clientId in the form Client_'id sent as argument'
     * @param iterationCount Number of iterations for transfer
     *
     */
    client(Properties prop, String clientId, int iterationCount, int numServers){
        this.iterationCount = iterationCount;
        this.prop = prop;
        this.clientId = "Client_"+clientId;
        this.numServers = numServers;
    }

    /**
     * Runs when start method is called on BankClient object
     */
    public void run() {

        for (int i=0;i<iterationCount;i++){
            Random rand = new Random();
            int n = rand.nextInt(5);
            String server = "Server_"+n;

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
            int rnd1 = 1 + new Random().nextInt(19); // random number between 1 and 20
            int rnd2 = 1 + new Random().nextInt(19); // random number between 1 and 20
            if (rnd1==rnd2) {
                continue;
            }
            try {
                String[] content = new String[6];
                content[0]=clientId;
                content[1]= server;
                content[2]=LocalDateTime.now().toString();
                content[4]="Transfer";
                content[5]=rnd1+","+rnd2+", 10";

                String logMsg = String.format("CLNT-ID: %s | SVR-ID: %s | REQ | Physical-clock-time: %s | Operation: %s | Operation-name: %s | Parameters: %s \n", (Object[]) content);
                writeToLog(clientId+"_log.txt",logMsg);

                status = bankServer.operate(clientId,server, rnd1,rnd2,10);
            } catch (RemoteException e) {
                throw new RuntimeException("RemoteException: "+e);
            }


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

        int numThreads = Integer.parseInt(args[1]);
        int iterationCount=200;

        List<client> clientList = new ArrayList<client>();
        int numServers = 3;
        for (int i = 0; i < numThreads; i++) {
            client bankClient = new client(prop, clientId, iterationCount, numServers);
            clientList.add(bankClient);
            bankClient.start();
        }

        sendHalt(clientList,prop);

    }

    public static void sendHalt(List<client> clientList, Properties prop) throws RemoteException {
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

            bankServer.halt();

        return;
    }

    /**
     * Writes content to log file. Critical section because multiple threads access the function and only one thread
     * should be able to write to the log file.
     * @param fileName Name of the log file
     * @param content Content to be written to the file
     */
    public synchronized static void writeToLog(String fileName, String content) {
        try {

            File oFile = new File(fileName);
            if (!oFile.exists()) {
                oFile.createNewFile();
            }
            if (oFile.canWrite()) {
                BufferedWriter oWriter = new BufferedWriter(new FileWriter(fileName, true));
                oWriter.write(content);
                oWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}