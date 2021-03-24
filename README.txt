### RMI-based-server

###### Authors:
1. Divya Nairy (5589575) - shrin020@umn.edu
2. Krishna Shravya Gade (5592616) - gade0030@umn.edu

###### CS lab Machine tested on:
csel-kh4250-09.cselabs.umn.edu
csel-kh4250-10.cselabs.umn.edu
csel-kh4250-11.cselabs.umn.edu
csel-kh4250-12.cselabs.umn.edu
csel-kh4250-13.cselabs.umn.edu

###### Instructions to run:
1. Start the rmiregistry on ports mentioned in config file (5000,5001,5002,5003,5004)

2. Compile the BankServerImpl.java file
   javac server.java

3. Compile the BankClient.java file
   javac client.java

4. Run server with arguments
   java -Djava.security.policy=mySecurityPolicyfile server <server-ID> <configFile> <numClients>
   eg: java -Djava.security.policy=mySecurityPolicyfile server 0 server.conf 3
   
5. Run client with arguments
   java  -Djava.security.policy=mySecurityPolicyfile -Dserver.port=<clientPortNumber> client <clientId> <threadCount> <configFile>
   eg: java -Djava.security.policy=mySecurityPolicyfile -Dserver.port=4001 client 1 15 server.conf

6. View results in clientLogfile.txt and severLogfile.txt files

###### Name of Log files
Server_<id>_log.txt
Client_<id>_log.tx

###### Known bugs
Server processes not exiting. This is mostly because we are not unbiding from the registry.