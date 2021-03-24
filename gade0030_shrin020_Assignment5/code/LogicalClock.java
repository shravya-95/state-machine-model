/**
 * Lamport clock implementation
 */
public class LogicalClock {
    private int localTime;
    private String serverId;
    private int processId;

    /**
     * LogicalClock constructor
     * @param serverId
     */
    public LogicalClock(String serverId){
        this.localTime=0;
        this.serverId=serverId;
    }
    //if event is local, increment localTime. If remote, update localTime

    /**
     * Update the time after any event/message communication
     * @return updated time
     */
    public int updateTime(){
            this.localTime++;
        return this.localTime;
    }

    /**
     * Updates the time according to the timestamp of the server which communicated with this server
     * @param timeStamp
     * @return updated time
     */
    public int updateTime(int timeStamp){
        this.localTime=Math.max(timeStamp, this.localTime) + 1;
        return this.localTime;
    }

    /**
     * Retrieve the time
     * @return localTime
     */
    public int getLocalTime(){
        return this.localTime;
    }

}
