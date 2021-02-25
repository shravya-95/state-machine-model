import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.*;
import java.util.Hashtable;

public class BankServerImpl implements BankServer {
  protected static Hashtable<Integer, Account> accounts;
  private static int uuidCount = 0;
  public BankServerImpl () throws RemoteException{
    super();
  }
  public int getNewUid(){
    return ++uuidCount;
  }
  class Account{
    public int uid;// unique Id for accounts - an integer sequence counter starting with 1
    int balance = 0;
    public Account(int uid){
      this.uid=uid;
    }
    public int withdraw(int amount) {

      this.balance = this.balance - amount;
      return this.balance;

    }
    public int deposit(int amount){
      this.balance = this.balance+amount;
      return this.balance;
    }
    public int getBalance(){
      return this.balance;
    }
  }
//  public synchronized boolean transfer(int target, int source, int amount) throws InterruptedException {
//    if(accounts.get(source).getBalance()<amount){
//      //write to log file
//      return false;
//    }
//    accounts.get(source).withdraw(amount);
//    accounts.get(target).deposit(amount);
//    String msg = "Transferred %d from %d to %d";
//    System.out.printf(msg,amount,source,target);
//    notifyAll();
//    return true;
//  }
//  public static synchronized void writeToLog(String fileName, String content) throws IOException {
//    try {
//
//      File oFile = new File(fileName);
//      if (!oFile.exists()) {
//        oFile.createNewFile();
//      }
//      if (oFile.canWrite()) {
//        BufferedWriter oWriter = new BufferedWriter(new FileWriter(fileName, true));
//        oWriter.write(content);
//        oWriter.close();
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//
//  public void run (){
//    String logMsg = "";
//    String[] content = new String[3];
//    try {
//      OutputStream out = s.getOutputStream();
//      ObjectOutputStream outstream = new ObjectOutputStream(out);
//      InputStream instream = s.getInputStream();
//      ObjectInputStream oinstream = new ObjectInputStream(instream);
//      Request request = (Request) oinstream.readObject();
//      String requestType = request.getRequestType();
//      System.out.println("Request type:" + requestType);
//      switch (requestType) {
//        case "createAccount": {
//          int uid = ((CreateAccountRequest) request).getNewUid();
//          Account account = new Account(uid);
//          accounts.put(uid, account);
//          Response createResponse = new CreateAccountResponse(uid);
//          outstream.writeObject(createResponse);
//
//          content[0]="createAccount";
//          content[1]="";
//          content[2]= String.valueOf(uid);
//          break;
//        }
//        case "deposit": {
//          DepositRequest depositRequest = (DepositRequest) request;
//          int uid = depositRequest.getUid();
//          Account account = accounts.get(uid);
//          if (account==null){
//            System.out.printf("Account uid %d not found",uid);
//            break;
//          }
//          account.deposit(depositRequest.getAmount()); //check if this updates or need to put again
//
//          Response createResponse = new DepositResponse(true);
//          outstream.writeObject(createResponse);
//          content[0]="deposit";
//          content[1]= "UID: "+uid + "," + "Amount:" + depositRequest.getAmount();
//          content[2]= String.valueOf(((DepositResponse) createResponse).getStatus());
//          break;
//        }
//        case "getBalance": {
//          GetBalanceRequest getBalanceRequest = (GetBalanceRequest) request;
//          int uid = getBalanceRequest.getUid();
//          Account account = accounts.get(uid);
//          if (account==null){
//            System.out.printf("Account uid %d not found",uid);
//            break;
//          }
//          Response getBalanceResponse = new GetBalanceResponse(account.getBalance());
//          outstream.writeObject(getBalanceResponse);
//          content[0]="getBalance";
//          content[1]="UID: "+uid;
//          content[2]= String.valueOf(((GetBalanceResponse) getBalanceResponse).getBalance());
//          break;
//        }
//        case "transfer": {
//          boolean status;
//          TransferRequest transferRequest = (TransferRequest) request;
//          int sourceUid = transferRequest.getSourceUid();
//          int targetUid = transferRequest.getTargetUid();
//          int amount = transferRequest.getAmount();
//          try {
//            status = this.transfer(targetUid, sourceUid, amount);
//          } catch (InterruptedException ex) {
//            status= false;
//            ex.printStackTrace();
//          }
//          Response transferResponse = new TransferResponse(status);
//          outstream.writeObject(transferResponse);
//          content[0]="transfer";
//          content[1]="From:"+ sourceUid +", To:"+ targetUid +", Amount:"+ amount;
//          content[2]= String.valueOf(((TransferResponse) transferResponse).getStatus());
//          break;
//        }
//        default:
//          throw new RuntimeException("Illegal request type");
//      }
//      logMsg = String.format("Operation: %s | Inputs: %s | Result: %s \n", (Object[]) content);
//      writeToLog("severLogfile.txt",logMsg);
////      System.out.println("Client exit.");
//    } catch (IOException ex) {
//      ex.printStackTrace();
//    } catch (ClassNotFoundException e) {
//      e.printStackTrace();
//    }
//      finally {
//      try {
//        System.out.println("Closing socket");
//        s.close();
//      } catch (IOException ex) {
//        ex.printStackTrace();
//      }
//    }
//  }

  public int createAccount(){
      int uid = getNewUid();
      Account account = new Account(uid);
      accounts.put(uid, account);
      return uid;
  }
  public boolean deposit(int uid, int amount){
    return false;
  }
  public int getBalance(int uid){
    return 0;
  }
  public boolean transfer(int sourceUid, int targetUid, int amount){
    return false;
  }

  public static void main (String args[]) throws Exception {
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }
    BankServerImpl  bankServer  = new BankServerImpl( );
    BankServer bankServerStub  =  (BankServer) UnicastRemoteObject.exportObject(bankServer, 0) ;

    if ( args.length == 0 ) {
      // If no port number is given for the rmiregistry, assume it is on the default port 1099
      Naming.bind ("BankServer", bankServerStub);
    }
    else {
      // rmiregistry is on port specified in args[0]. Bind to that registry.
      Registry localRegistry = LocateRegistry.getRegistry( Integer.parseInt( args[0] ));
      localRegistry.bind ("BankServer", bankServerStub);
    }

    accounts = new Hashtable<>();

////    System.out.println ("Starting on port " + args[0]);
//    ServerSocket server = new ServerSocket (Integer.parseInt (args[0]));
//
//    while (true) {
//      System.out.println ("........Waiting for a client request");
//      Socket client = server.accept ();
////      System.out.println( "Received request from " + client.getInetAddress ());
////      System.out.println( "Starting worker thread..." );
//      BankServer bankServer = new BankServer(client);
//      bankServer.start();
//    }

  }
}
