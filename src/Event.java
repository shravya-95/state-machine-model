import java.time.LocalDateTime;

public class Event {
/* 0 - CLIENT_REQ
    1 - SRV_REQ
    2 - REQ_PROCESSING
    3 - REQ
    4 - RSP
 */
    public int type;
    public long senderId;
    public long receiverId;
    public int timeStamp;
    public String content;
    public boolean remote;
    public LocalDateTime physicalClock;

    public Event(int type, long senderId, long receiverId, String content) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timeStamp = 0;
    }

    public Event(int type, long senderId,
                 long receiverId, int timeStamp, boolean remote,LocalDateTime physicalClock, String content) {
        this(type, senderId, receiverId, content);
        this.timeStamp = timeStamp;
        this.remote=remote;
        this.physicalClock=physicalClock;
    }

}