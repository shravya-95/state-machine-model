
public class LogicalClock {
    private int localTime;
    private String serverId;
    private int processId;

    public LogicalClock(String serverId){
        this.localTime=0;
        this.serverId=serverId;
    }
    //if even is local, increment localTime. If remote, update localTime

    public int updateTime(){
            this.localTime++;
        return this.localTime;
    }
    public int updateTime(int timeStamp){
        this.localTime=Math.max(timeStamp, this.localTime) + 1;
        return  this.localTime;
    }

    public int getLocalTime(){
        return this.localTime;
    }

}
