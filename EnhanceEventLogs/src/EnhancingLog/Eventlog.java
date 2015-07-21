/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EnhancingLog;

import java.sql.Timestamp;

/**
 *
 * @author nesma
 */
public class Eventlog {
    
    private int event_id = 0;
    private int case_id = 0;
    private String resources = null;
    private String activity = null;
    private String eventType = null;
    private Timestamp  time = null;

    /**
     * @return the event_id
     */
    public int getEvent_id() {
        return event_id;
    }

    /**
     * @param event_id the event_id to set
     */
    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    /**
     * @return the case_id
     */
    public int getCase_id() {
        return case_id;
    }

    /**
     * @param case_id the case_id to set
     */
    public void setCase_id(int case_id) {
        this.case_id = case_id;
    }

    /**
     * @return the resources
     */
    public String getResources() {
        return resources;
    }

    /**
     * @param resources the resources to set
     */
    public void setResources(String resources) {
        this.resources = resources;
    }

    /**
     * @return the activity
     */
    public String getActivity() {
        return activity;
    }

    /**
     * @param activity the activity to set
     */
    public void setActivity(String activity) {
        this.activity = activity;
    }

    /**
     * @return the eventType
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * @param eventType the eventType to set
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
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
