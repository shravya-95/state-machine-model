
public class LamportClock {
    private int localTime;
    private int serverId;
    private int processId;

    public LamportClock(int localTime, int serverId, int processId){
        this.localTime=localTime;
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