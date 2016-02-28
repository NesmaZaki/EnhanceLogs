/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RepairLog;

import java.sql.Timestamp;

/**
 *
 * @author nesma
 */
public class Holder {
       
    private String first_activity = null;
    private String second_activity = null;
    private String Intervals = null;
    private Timestamp time = null;

    /**
     * @return the first_activity
     */
    public String getFirst_activity() {
        return first_activity;
    }

    /**
     * @param first_activity the first_activity to set
     */
    public void setFirst_activity(String first_activity) {
        this.first_activity = first_activity;
    }

    /**
     * @return the second_activity
     */
    public String getSecond_activity() {
        return second_activity;
    }

    /**
     * @param second_activity the second_activity to set
     */
    public void setSecond_activity(String second_activity) {
        this.second_activity = second_activity;
    }

    /**
     * @return the Intervals
     */
    public String getIntervals() {
        return Intervals;
    }

    /**
     * @param Intervals the Intervals to set
     */
    public void setIntervals(String Intervals) {
        this.Intervals = Intervals;
    }

    /**
     * @return the time
     */
    public Timestamp getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(Timestamp time) {
        this.time = time;
    }
}
