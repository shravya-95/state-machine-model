import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.*;
import java.util.Hashtable;
import java.io.*;
import java.util.Properties;
import java.net.InetAddress;

public class BankServerImpl implements BankServer {
  //hashtable to hold the account's uid and object
  protected static Hashtable<Integer, Account> accounts;
  private static int uuidCount = 0;

  public BankServerImpl () throws RemoteException{
    super();
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
    synchronized (this){
      accounts.get(sourceUid).withdraw(amount);
      accounts.get(targetUid).deposit(amount);
      String msg = "Transferred %d from %d to %d\n";
      System.out.printf(msg,amount,sourceUid,targetUid);
      notifyAll();
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
   private static void serverInitialize() {
    //create 20 accounts
    //add 1000 to all accounts
    //print init complete
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
    if ( args.length < 2 ) {
      throw new RuntimeException( "Syntax: java server server-ID configFile numClients" );
    }
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }

    String serverId = "Server_"+args[0];

    String configFileName = args[1];
    Properties prop = loadConfig(configFileName);

    BankServerImpl  bankServer  = new BankServerImpl( );

    System.setProperty("java.rmi.server.hostname",  InetAddress.getLocalHost().getHostName());
    BankServer bankServerStub  =  (BankServer) UnicastRemoteObject.exportObject(bankServer, Integer.parseInt(prop.getProperty(serverId+".port")));
    Registry localRegistry = LocateRegistry.getRegistry(Integer.parseInt(prop.getProperty(serverId+".rmiregistry")));
    localRegistry.bind (serverId, bankServerStub);
    accounts = new Hashtable<>();
    LogicalClock logicalClock = new LogicalClock(serverID, processID);
    serverInitialize();
  }


}
