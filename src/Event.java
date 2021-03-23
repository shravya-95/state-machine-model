import java.io.Serializable;
import java.time.LocalDateTime;


public class Event extends Object implements Serializable{
/* 0 - CLIENT_REQ
    1 - SRV_REQ
    2 - HALT
    3 - EXECUTE
 */
    public int type;
    public String senderId;
    public String receiverId;
    public int timeStamp;
    public String content;
    public boolean remote;
    public LocalDateTime physicalClock;
    public int clientTimeStamp;
    public int serverReceivedClient=Integer.MAX_VALUE;


    public Event(int type, String senderId, String receiverId, String content)  {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timeStamp = 0;
    }
    public Event(int type, String senderId,
                 String receiverId, int timeStamp, int clientTimeStamp, boolean remote,LocalDateTime physicalClock, String content) {
        this(type, senderId, receiverId, content);
        this.timeStamp = timeStamp;
        this.remote=remote;
        this.physicalClock=physicalClock;
        this.clientTimeStamp=clientTimeStamp;
    }

    public Event(int type, String senderId,
                 String receiverId, int timeStamp, int clientTimeStamp, int serverReceivedClient, boolean remote,LocalDateTime physicalClock, String content) {
        this(type, senderId, receiverId, content);
        this.timeStamp = timeStamp;
        this.remote=remote;
        this.physicalClock=physicalClock;
        this.clientTimeStamp=clientTimeStamp;
        this.serverReceivedClient=serverReceivedClient;
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof Event){
            Event c = (Event) o;
            System.out.println("Timestamps -----"+this.clientTimeStamp+"---"+ c.clientTimeStamp);
            return this.clientTimeStamp== c.clientTimeStamp && this.serverReceivedClient==c.serverReceivedClient;
        }
        return false;
    }
    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }
    public void setPhysicalClock(){
        this.physicalClock=LocalDateTime.now();
    }
}