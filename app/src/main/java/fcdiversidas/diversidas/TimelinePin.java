package fcdiversidas.diversidas;

/**
 * Created by Zachary Bys on 2018-01-20.
 */

public class TimelinePin {
    private long timestamp;
    public String pinid;
    private String type;

    TimelinePin(long time, String id, String t){
        timestamp = time;
        pinid = id;
        type = t;
    }
}
