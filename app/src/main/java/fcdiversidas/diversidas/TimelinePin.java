package fcdiversidas.diversidas;

/**
 * Created by Zachary Bys on 2018-01-20.
 */

public class TimelinePin {
    private long timestamp;
    private int pinid;
    private String type;

    TimelinePin(long time, int id, String t){
        timestamp = time;
        pinid = id;
        type = t;
    }
}
