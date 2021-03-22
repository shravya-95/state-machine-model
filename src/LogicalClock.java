
public class LogicalClock {
    private int localTime;
    private int serverId;
    private int processId;

    public LogicalClock(int serverId, int processId){
        this.localTime=0;
        this.serverId=serverId;
        this.processId=processId;
    }

    public int updateTime(Event event){
        //if even is local, increment localTime. If remote, update localTime
        if (event.remote)
            this.localTime=Math.max(event.timeStamp, this.localTime) + 1;
        else
            this.localTime++;
        return this.localTime;
    }

    public int getLocalTime(){
        return this.localTime;
    }

}
