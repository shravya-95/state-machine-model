import java.rmi.RMISecurityManager;
import java.rmi.Naming;
import java.util.Date;

public class BankClient {
    public static void main (String args[]) throws Exception {
        if ( args.length != 2 ) {
            throw new RuntimeException( "Syntax: java BankClient serverHostname severPortnumber" );
        }
        System.setSecurityManager (new SecurityManager ());
        DateServer bankServer = (BankServer) Naming.lookup ("//" + args[0] + "/DateServer");
        //Same as before here

    }
}