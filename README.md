### RMI-based-server
###### Authors:
1. Divya Nairy (5589575) - shrin020@umn.edu
2. Krishna Shravya Gade (5592616) - gade0030@umn.edu
###### Instructions to run:
1. Start the rmiregistry on any port. If port not provided, it will run on default port 1099:
~~~
rmiregistry <port>
~~~
2. Compile the BankServerImpl.java file
~~~
javac BankServerImpl.java
~~~
3. Compile the BankClient.java file
~~~
javac BankClient.java
~~~
4. Run BankServerImpl and give input as port RMI is running.
If port not provided, default RMI port 1099 is taken.
~~~
java BankServerImpl <port>
~~~
5. Run BankClient
~~~
java BankClient <serverHostname> <severPortnumber> <threadCount> <iterationCount>
~~~
6. View results in clientLogfile.txt and severLogfile.txt files
