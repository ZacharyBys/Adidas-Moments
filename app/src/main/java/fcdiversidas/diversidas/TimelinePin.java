package fcdiversidas.diversidas;

/**
 * Created by Zachary Bys on 2018-01-20.
 */

public class TimelinePin {
    public long timestamp;
    public String pinid;
    private String type;
    public int numberOfReactions;

    TimelinePin(long time, String id, String t){
        timestamp = time;
        pinid = id;
        type = t;
    }
}
