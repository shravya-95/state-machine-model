import java.rmi.RMISecurityManager;
import java.rmi.Naming;
import java.util.Date;

//java  -Djava.security.policy=mySecurityPolicyfile BankClient localhost 12345
public class BankClient {
    public static void main (String args[]) throws Exception {
        if ( args.length != 2 ) {
            throw new RuntimeException( "Syntax: java BankClient serverHostname severPortnumber" );
        }
        System.setSecurityManager (new SecurityManager ());
        BankServer bankServer = (BankServer) Naming.lookup ("//" + args[0] + ":"+ args[1]+"/BankServer");
        //Same as before here
        int uid1 = bankServer.createAccount();
//        int uid2 = bankServer.createAccount();

        System.out.println(bankServer.deposit(uid1,100));
        System.out.println(bankServer.getBalance(uid1));

    }
}