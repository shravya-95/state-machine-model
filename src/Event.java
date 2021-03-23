import java.io.Serializable;
import java.time.LocalDateTime;

public class Event implements Serializable{
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

    public Event(int type, String senderId, String receiverId, String content) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timeStamp = 0;
    }

    public Event(int type, String senderId,
                 String receiverId, int timeStamp, boolean remote,LocalDateTime physicalClock, String content) {
        this(type, senderId, receiverId, content);
        this.timeStamp = timeStamp;
        this.remote=remote;
        this.physicalClock=physicalClock;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }
    public void setPhysicalClock(){
        this.physicalClock=LocalDateTime.now();
    }
}