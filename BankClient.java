import java.rmi.RMISecurityManager;
import java.rmi.Naming;
import java.util.Date;

public class BankClient {
    public static void main (String args[]) throws Exception {
        if ( args.length != 2 ) {
            throw new RuntimeException( "Syntax: java BankClient serverHostname severPortnumber" );
        }
        System.setSecurityManager (new SecurityManager ());
        BankServer bankServer = (BankServer) Naming.lookup ("//" + args[0] + ":"+ args[1]+"/BankServer");
        //Same as before here
        bankServer.createAccount();
    }
}