import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.*;
import java.time.LocalDateTime;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.concurrent.PriorityBlockingQueue;

public class server extends Thread implements BankServer, BankReplica {
  //hashtable to hold the account's uid and object
  protected static Hashtable<Integer, Account> accounts;
  private static int uuidCount = 0;
  private String msg;
  public static PriorityBlockingQueue<Event> eventQueue;
  public static LogicalClock logicalClock;
  private static Properties prop;
  private static String serverId;
  private static int haltedClients=0;
  private Object lock = new Object();
  private static int numClients;
  private static int[] uids;
  public server  bankServer;
  public static List<String> mockQ;
  public server () throws RemoteException{
    super();

  }



  public void run(){
    while(true){
      while (!eventQueue.isEmpty()){
//      System.out.println("QUEUE CONTENT"+eventQueue.poll().timeStamp);
//        System.out.println("PEEKING PQ"+"--------"+eventQueue.peek().serverReceivedClient+"--------"+eventQueue.peek().clientTimeStamp);
        try {
          pollQueue();
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }
      try {
        sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }


  public int getNewUid(){
    return ++uuidCount;
  }

  /**
   * Account Class
   * attributes: uid and account
   * methods: withdraw, deposit, getBalance
   */
  class Account{
    public int uid;// unique Id for accounts - an integer sequence counter starting with 1
    int balance = 0;

    public Account(int uid){
      this.uid=uid;
    }

    /**
     * Withdraws given amount from account by subtracting from balance
     * @param amount Amount to be withdrawn
     * @return new balance
     */
    public int withdraw(int amount) {
      this.balance = this.balance - amount;
      return this.balance;

    }

    /**
     * Deposit given amount to account by adding to balance
     * @param amount Amount to be deposited
     * @return new balance
     */
    public int deposit(int amount){
      this.balance = this.balance+amount;
      return this.balance;
    }

    /**
     * Getter method to access balance
     * @return balance
     */
    public int getBalance(){
      return this.balance;
    }
  }

  public void halt() throws RemoteException {
    int uts = logicalClock.updateTime();
    //communicate here
    synchronized (lock){
      haltedClients++;
      System.out.println("HALTED CLIENT"+haltedClients+"----Total clients"+numClients);
    }
    if (haltedClients==numClients){
      System.out.println("numClients equal");
      //send it to everyone

      Event haltEvent = new Event(2,serverId,"null",uts,uts,true,LocalDateTime.now(),"HALT!");
      sendMulticast(haltEvent);
      //empty my queue

      //TODO: RMI connection should be unbound and closed
      while (!eventQueue.isEmpty());
      getTotalBalance();
      System.out.println("ALL DONE!----- READY TO EXIT");

    }
    return;
  }

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

  public int createAccount(){
    int uid = getNewUid();
    Account account = new Account(uid);
    accounts.put(uid, account);
    String[] content = new String[3];
    content[0]="createAccount";
    content[1]="";
    content[2]= String.valueOf(uid);
    String logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
    writeToLog("severLogfile.txt",logMsg);
    return uid;
  }

  public boolean deposit(int uid, int amount){
    Account account = accounts.get(uid);
    boolean status = true;
    if (account==null){
        System.out.printf("Account uid %d not found",uid);
        status = false;
    }
    account.deposit(amount);
    String[] content = new String[3];
    content[0]="deposit";
    content[1]= "UID: "+uid + "," + "Amount:" + amount;
    content[2]= String.valueOf(status);
    String logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
    writeToLog("severLogfile.txt",logMsg);
    return status;
  }
  public int getBalance(int uid){
      Account account = accounts.get(uid);
      if (account==null){
        System.out.printf("Account uid %d not found",uid);
        return -1;
      }
      int balance = account.getBalance();
      String[] content = new String[3];
      content[0]="getBalance";
      content[1]="UID: "+uid;
      content[2]= String.valueOf(balance);
      String logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
      writeToLog("severLogfile.txt",logMsg);
      return balance;
  }

  /**
   * Request sent by client to initiate transfer
   */
  public boolean operate(String clientId,String serverId, int sourceUid, int targetUid, int amount) throws RemoteException {
    Event clientReq = new Event(0,clientId,serverId,sourceUid+","+targetUid+","+amount);
    int currServerTs = logicalClock.updateTime();
    clientReq.setTimeStamp(currServerTs);
    clientReq.clientTimeStamp=currServerTs;
    clientReq.serverReceivedClient=Integer.parseInt(serverId.substring(7));
    System.out.println("OPERATE TS -----"+currServerTs);
    clientReq.setPhysicalClock();


    //here, multicast message
    sendMulticast(clientReq);
    eventQueue.add(clientReq);
    //change below
//    pollQueue();
    return true ;
  }

  private boolean pollQueue() throws RemoteException {
    if (!eventQueue.isEmpty()){

//      System.out.print(serverId+"pollQueue");
      Event currHead = eventQueue.peek();
//      System.out.println(currHead.type+ "-------- sender ID"+currHead.senderId+"----- receiver ID"+ currHead.receiverId+"-----Client time stamp"+currHead.clientTimeStamp+"-------Server Received from Client"+currHead.serverReceivedClient);

      if (currHead!=null && currHead.senderId.contains("Client")){
        String[] msg = currHead.content.split(",");
        System.out.println("---EXECUTING TRANSFER----"+currHead.senderId+"---"+currHead.receiverId);
        transfer(Integer.parseInt(msg[0]),Integer.parseInt(msg[1]),Integer.parseInt(msg[2]));
        System.out.println("POLL QUEUE CLIENT MESSAGE" + currHead.clientTimeStamp);
        eventQueue.poll();
//      if (!result){
//        System.out.println("UNABLE TO REMOVE"+serverId);
//      }
        currHead.type=3;
        currHead.senderId=serverId;
        sendMulticast(currHead);
      }

    }

    return true;
  }

  /**
   * Use the RMI groupserver stub to multicast messages
   */
  public int receiveRequest(String msg, Event request) throws RemoteException{
//    Event replicaEvent = new Event(1, request.receiverId, serverId,logicalClock.updateTime(request.timeStamp),1, LocalDateTime.now(),request.content);
//    request.type=1;
//    request.receiverId=serverId;
    logicalClock.updateTime(request.timeStamp);
    eventQueue.add(request);
//    mockQ.add(request.receiverId+","+request.senderId+","+request.clientTimeStamp+","+request.serverReceivedClient);

    return logicalClock.updateTime();
  }
  public void receiveExecute(Event removeEvent) throws RemoteException{

    System.out.println("receiveExecute REMOVE EVENT TS---"+removeEvent.clientTimeStamp);
    boolean result = eventQueue.remove(removeEvent);
//    System.out.println("REMOVED EVENT"+result.serverReceivedClient+"--"+removeEvent.serverReceivedClient+"------------" + result.clientTimeStamp+"---"+removeEvent.clientTimeStamp);
    if (!result){
      System.out.println("FAILED TO REMOVE AFTER MULTICAST");
    }
    String[] msg = removeEvent.content.split(",");
    System.out.println("--- receiveExecute --- EXECUTING TRANSFER----"+removeEvent.senderId+"---"+serverId);
    transfer(Integer.parseInt(msg[0]),Integer.parseInt(msg[1]),Integer.parseInt(msg[2]));


  }
  public void receiveHalt(Event clientReq) throws RemoteException {
    System.out.println("Received Halt"+serverId);
    while (!eventQueue.isEmpty());
    System.out.println("finished polling"+serverId);
    getTotalBalance();
//    System.exit(0);
    System.out.println("REMOTE HAT DONE!-----READY TO EXIT");
  }


  public void sendMulticast(Event clientReq) throws RemoteException {
    for(int i=0;i<5;i++){
//      for(int i=0;i<5;i++){
        String replicaId = "Server_"+i;

        if (replicaId.equals(serverId)) continue;
        System.out.println(serverId+ "MULTICASTING TO Server_"+i + "--- sendMulticast----"+clientReq.senderId+"---"+clientReq.receiverId);
      Registry registry = null;
        BankReplica backReplica;
        try {
          registry = LocateRegistry.getRegistry(prop.getProperty(replicaId+".hostname"), Integer.parseInt(prop.getProperty(replicaId+".rmiregistry")));
        } catch (RemoteException e) {
          throw new RuntimeException("RemoteException before HALT: "+e);
        }
        try {
          backReplica = (BankReplica) registry.lookup("Replica_"+i);
        } catch (RemoteException e) {
          throw new RuntimeException("RemoteException before HALT: "+e);
        } catch (NotBoundException e) {
          throw new RuntimeException("NotBoundException before HALT: "+e);
        }
        logicalClock.updateTime();
        if (clientReq.type==0) {
          Event multicastEvent = new Event(1,serverId,"Server_"+i,logicalClock.getLocalTime(),clientReq.clientTimeStamp,clientReq.serverReceivedClient,true,LocalDateTime.now(),clientReq.content);
          System.out.println("CLIENT REQ TIME STAMP multicastEvent "+clientReq.clientTimeStamp);
          System.out.println("Server_"+i + "--- clientReq type 0 ----"+clientReq.senderId+"---"+clientReq.receiverId);
          int replicaTimestamp = backReplica.receiveRequest(serverId, multicastEvent);
          //logging ack??
          logicalClock.updateTime(replicaTimestamp);
        }
        else if (clientReq.type==3){
          //remove from pq
          System.out.println("Server_"+i + "--- clientReq type 3 ----"+clientReq.senderId+"---"+clientReq.receiverId);
          backReplica.receiveExecute(clientReq);
          //transfer function
        }
        else if (clientReq.type==2){
          System.out.println("Server_"+i + "--- clientReq type 2 --- HALT ----"+clientReq.senderId+"---"+clientReq.receiverId);
          backReplica.receiveHalt(clientReq);
        }
      }
  }
  public static int getTotalBalance() {
    int numAccounts = 20;
    int total = 0;
    for (int i = 1; i <= numAccounts; i++) {
      String logMsg = "";
      String[] content = new String[3];
      int balance = accounts.get(uids[i]).getBalance();
      total += balance;
      content[0]="getTotalBalance";
      content[1]= "UID: "+ uids[i];
      content[2]= "AccountBalance: "+ balance +", Total so far:"+total;
      logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
      writeToLog("clientLogfile.txt",logMsg);
    }
    System.out.println(serverId+": total= "+total);
    return total;
  }
  /**
   * Transfer method to transfer balance from source account to target account. This method is synchronized to access critical sections.
   * @parameters target(uid of target account), source(uid of source account) and amount(to be transferred)
   * @return status(true for successful transfer, false for unsuccessful)
   */
  public boolean transfer(int sourceUid, int targetUid, int amount){
    if(!accounts.containsKey(sourceUid)){
      writeToLog("severLogfile.txt", "Accounts doesn't have key"+String.valueOf(sourceUid));
    }
    //insufficient balance
    if(accounts.get(sourceUid).getBalance()<amount){
        String[] content = new String[3];
        content[0]="transfer";
        content[1]="From:"+ sourceUid +", To:"+ targetUid +", Amount:"+ amount;
        content[2]= "False";
        String logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
        writeToLog("severLogfile.txt",logMsg);
      return false;
    }
    //transfer
    synchronized (accounts){
      accounts.get(sourceUid).withdraw(amount);
      accounts.get(targetUid).deposit(amount);
      String msg = "Transferred %d from %d to %d\n";
      System.out.printf(msg,amount,sourceUid,targetUid);
      accounts.notifyAll();
    }


    //logging
    String[] content = new String[3];
    content[0]="transfer";
    content[1]="From:"+ sourceUid +", To:"+ targetUid +", Amount:"+ amount;
    content[2]= "True";
    String logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
    writeToLog("severLogfile.txt",logMsg);
    return true;
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

  public static void main(String args[]) throws Exception {
    if ( args.length < 3 ) {
      throw new RuntimeException( "Syntax: java server server-ID configFile numClients" );
    }
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }

    serverId = "Server_"+args[0];
    numClients=Integer.parseInt(args[2]);
    logicalClock = new LogicalClock(serverId);
    eventQueue = new PriorityBlockingQueue<Event>();

    String configFileName = args[1];
    prop = loadConfig(configFileName);

    server bankServer  = new server( );
    System.setProperty("java.rmi.server.hostname",  InetAddress.getLocalHost().getHostName());
    BankServer bankServerStub  =  (BankServer) UnicastRemoteObject.exportObject(bankServer, Integer.parseInt(prop.getProperty(serverId+".port")));
    Registry localRegistry = LocateRegistry.getRegistry(Integer.parseInt(prop.getProperty(serverId+".rmiregistry")));
    localRegistry.bind (serverId, bankServerStub);

    accounts = new Hashtable<>();
    serverInitialize(bankServer);
    System.out.println("Server initialization is complete");

    for(int i=0;i<5;i++){
//    for(int i=0;i<5;i++){
//      BankReplica bankReplicaStub  =  (BankReplica) UnicastRemoteObject.exportObject(bankServer, Integer.parseInt(prop.getProperty("Server_"+i+".port")));
      BankReplica bankReplicaStub  =  (BankReplica) bankServerStub;
      Registry localRegistry1 = LocateRegistry.getRegistry(Integer.parseInt(prop.getProperty(serverId+".rmiregistry")));
      localRegistry.bind ("Replica_"+i, bankReplicaStub);
    }
    bankServer.start();
  }

  private static void serverInitialize(BankServer bankServer) throws RemoteException {
    int[] uid_array = createAccounts(20, bankServer);
    deposit(uid_array, 1000, 20, bankServer);
    uids = uid_array;

  }

  /**
   * Creates mentioned number of accounts
   * @param numAccounts Total number of accounts
   * @param bankServer BankServer RMI object
   * @return List of UIDs of the accounts created
   * @throws RemoteException When communication related exception occurs
   */
  private static int[] createAccounts(int numAccounts, BankServer bankServer) throws RemoteException {
    int[] uids = new int[numAccounts+1];//changed
    for (int i = 1; i <= numAccounts; i++) {
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
      for (int i = 1; i <= numAccounts; i++) {
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
}
