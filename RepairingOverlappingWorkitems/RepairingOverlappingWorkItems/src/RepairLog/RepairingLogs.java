/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RepairLog;

import static RepairLog.ConnDB.conn;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.algo.tree.rpst.RPSTNode;
import org.jbpt.algo.tree.tctree.TCType;
import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.RelSetType;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.Marking;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.pm.Activity;
import org.jbpt.pm.AndGateway;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.Gateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.XorGateway;

/**
 *
 * @author nesma
 */
public class RepairingLogs {

    int max = 0;
    ArrayList<Integer> positions = new ArrayList<Integer>();
    int counter = 0;

    public void mapping() throws ClassNotFoundException, InstantiationException, SQLException {
        ConnDB c = new ConnDB();
        c.connectDB();
        String data = " select eventtype,event_id from eventlog";
        PreparedStatement preStat = conn.prepareStatement(data);
        ResultSet rs1 = preStat.executeQuery();
        while (rs1.next()) {
            String insert = "update eventlog set absState = ? where event_id = ? ";
            PreparedStatement ps = conn.prepareStatement(insert);
            ps.setInt(2, rs1.getInt(2));
            String type = rs1.getString(1);
            if (type.equalsIgnoreCase("create") || type.equalsIgnoreCase("allocate") || type.equalsIgnoreCase("complete")
                    || type.equalsIgnoreCase("offer") || type.equalsIgnoreCase("fail")
                    || type.equalsIgnoreCase("suspend")) {
                ps.setString(1, "Idle");
            } else {
                ps.setString(1, "Active");
            }
            ps.executeUpdate();
        }
    }

    public void CT(String firstStrategy,String secondStrategy) throws SQLException, ClassNotFoundException, InstantiationException, IOException, ParseException {
        Eventlog e = new Eventlog();
        String Resource = null;
        ArrayList<Eventlog> data = new ArrayList<Eventlog>();
        ArrayList<String> resources = new ArrayList<String>();
        ArrayList<String> activities = new ArrayList<String>();
        max = maxEvent();
        String res = "select distinct resources from eventlog";
        PreparedStatement preStat = conn.prepareStatement(res);
        ResultSet rs1 = preStat.executeQuery();
        
        while (rs1.next()) {
            resources.add(rs1.getString(1));
        }

        for (int j = 0; j < resources.size(); j++) {
            data.clear();
            activities.clear();
            String detail = "select case_id,activity,resources,eventtype,Time_stamp,absState,event_id from eventlog where resources = ? order by time_stamp";
            PreparedStatement pre = conn.prepareStatement(detail);
            pre.setString(1, resources.get(j));
            ResultSet rs2 = pre.executeQuery();
            while (rs2.next()) {
                e.setActivity(rs2.getString(2));
                e.setCase_id(rs2.getInt(1));
                e.setEventType(rs2.getString(4));
                e.setResources(rs2.getString(3));
                e.setTime(rs2.getTimestamp(5));
                e.setAbstractState(rs2.getString(6));
                e.setEvent_id(rs2.getInt(7));
                data.add(e);
                e = new Eventlog();
            }
            String activity = "select distinct activity from eventlog where resources = ? order by time_stamp";
            PreparedStatement pre1 = conn.prepareStatement(activity);
            pre1.setString(1, resources.get(j));
            ResultSet rs = pre1.executeQuery();
            while (rs.next()) {
                activities.add(rs.getString(1));
            }
            Resource = resources.get(j);
            decideActivities(data, activities, Resource,firstStrategy,secondStrategy);
        }
    }

    public void recurseOnSpecificResource(String resource,String firstStrategy,String secondStrategy) throws SQLException, ClassNotFoundException, InstantiationException, IOException, ParseException {
        
        Eventlog e = new Eventlog();
        ArrayList<Eventlog> data = new ArrayList<Eventlog>();
        ArrayList<String> activities = new ArrayList<String>();
        String detail = "select case_id,activity,resources,eventtype,Time_stamp,absState,event_id from eventlog where resources = ? order by time_stamp";
        PreparedStatement pre = conn.prepareStatement(detail);
        pre.setString(1, resource);
        ResultSet rs2 = pre.executeQuery();
        while (rs2.next()) {
            e.setActivity(rs2.getString(2));
            e.setCase_id(rs2.getInt(1));
            e.setEventType(rs2.getString(4));
            e.setResources(rs2.getString(3));
            e.setTime(rs2.getTimestamp(5));
            e.setAbstractState(rs2.getString(6));
            e.setEvent_id(rs2.getInt(7));
            data.add(e);
            e = new Eventlog();
        }
        String activity = "select distinct activity from eventlog where resources = ? order by time_stamp";
        PreparedStatement pre1 = conn.prepareStatement(activity);
        pre1.setString(1, resource);
        ResultSet rs = pre1.executeQuery();
        while (rs.next()) {
            activities.add(rs.getString(1));
        }
     
        decideActivities(data, activities, resource,firstStrategy,secondStrategy);
    }

    public void decideActivities(ArrayList<Eventlog> data, ArrayList<String> activities, String resource,String firststrategy, String secondstrategy) throws SQLException, ClassNotFoundException, InstantiationException, IOException, ParseException {
        ArrayList<Holder> activities_type = new ArrayList<Holder>();
        activities_type = activityRelationShips(data, activities);
        activities.clear();
        if (activities_type.isEmpty() && positions.isEmpty()) {
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = dateFormat.parse("1555-12-01 01:00:00.0");
        long timest = date.getTime();
        Timestamp min_temp = new Timestamp(timest);
        String firstActivity = null;
        String secondActivity = null;
        if (counter > 0) {
            for (int i = 0; i < positions.size(); i++) {
                if (min_temp.before(activities_type.get(positions.get(i)).getTime()) == true || min_temp.equals(activities_type.get(positions.get(i)).getTime()) == true) {
                    min_temp = activities_type.get(positions.get(i)).getTime();
                    firstActivity = activities_type.get(positions.get(i)).getFirst_activity();
                    secondActivity = activities_type.get(positions.get(i)).getSecond_activity();
                    break;
                }
            }
            
            ArrayList<Eventlog> filteringData = filter_data(data, firstActivity, secondActivity);
            if(firststrategy.equalsIgnoreCase("COF")){
                closeOldItem(filteringData);
            }if(firststrategy.equalsIgnoreCase("DL")){
                deferLowPriorityItems(filteringData);
            }
            recurseOnSpecificResource(resource,firststrategy,secondstrategy);

        } else {
            DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = dateFormat1.parse("2015-12-02 13:15:00.0");
            long timest1 = date1.getTime();
            Timestamp temp = new Timestamp(timest1);
                
            for (int i = 0; i < activities_type.size(); i++) {
                if (temp.after(activities_type.get(i).getTime())) {
                    temp = activities_type.get(i).getTime();
                    firstActivity = activities_type.get(i).getFirst_activity();
                    secondActivity = activities_type.get(i).getSecond_activity();
                }
                if(temp.equals(activities_type.get(i).getTime())){
                    temp = activities_type.get(i).getTime();
                    firstActivity = activities_type.get(i).getFirst_activity();
                    secondActivity = activities_type.get(i).getSecond_activity();
                }
            }
            
            ArrayList<Eventlog> filteringData = filter_data(data, firstActivity, secondActivity);
     
            if(secondstrategy.equalsIgnoreCase("COF")){
                closeOldItem(filteringData);
            }if(secondstrategy.equalsIgnoreCase("SL")){
                suspendLowPriorityItems(filteringData, firstActivity, secondActivity);
            }
            recurseOnSpecificResource(resource,firststrategy,secondstrategy);
        }
    }

    public ArrayList<Holder> activityRelationShips(ArrayList<Eventlog> data, ArrayList<String> activities) throws SQLException, ClassNotFoundException, InstantiationException, IOException {
        // declaration of variables
        ArrayList<Holder> a = new ArrayList<Holder>();
        positions.clear();
        counter = 0;
        Holder h = new Holder();
        a.clear();
        
        int index = 0;
        int indexes = -1;
        boolean flag = false;
        
        ArrayList<String> backupActivity = new ArrayList<String>();
        ArrayList<Integer> backupIndex = new ArrayList<Integer>();
        
        for (int q = 0; q < activities.size(); q++) {
            for (int y = 0; y < data.size() - 1; y++) {
                if (data.get(y).getActivity().equalsIgnoreCase(activities.get(q)) == true && data.get(y).getAbstractState().equalsIgnoreCase("Active") == true) {
                    backupActivity.add(activities.get(q));
                    backupIndex.add(y);
                }
            }
        }

        for (int l = 0; l < backupActivity.size(); l++) {
            for (int k = backupIndex.get(l); k < data.size(); k++) {
                if (data.get(k).getActivity().equalsIgnoreCase(backupActivity.get(l)) == false) {
                    if (data.get(k).getAbstractState().equalsIgnoreCase("Active") == true) {
                        for (int i = k + 1; i < data.size(); i++) {
                            if (data.get(i).getActivity().equalsIgnoreCase(data.get(k).getActivity()) == true
                                    && data.get(i).getAbstractState().equalsIgnoreCase("Idle") == true) {
                                flag = true;
                                break;
                            } else if (data.get(i).getActivity().equalsIgnoreCase(backupActivity.get(l)) == true
                                    && data.get(i).getAbstractState().equalsIgnoreCase("Idle") == true) {
                                break;
                            }
                        }// end loop

                        if (flag == true) {
                            flag = false;
                            h.setFirst_activity(backupActivity.get(l));
                            h.setSecond_activity(data.get(k).getActivity());
                            h.setIntervals("di");
                            h.setTime(data.get(k).getTime());
                            a.add(h);
                            indexes = indexes + 1;
                            h = new Holder();
                        } else {
                            h.setFirst_activity(backupActivity.get(l));
                            h.setSecond_activity(data.get(k).getActivity());
                            h.setIntervals("o");
                            h.setTime(data.get(k).getTime());
                            a.add(h);
                            indexes = indexes + 1;
                            positions.add(indexes);
                            counter++;
                            h = new Holder();
                        }// end flag 
                    } // close if "Active"
                    else {
                        continue;
                    }// in case of idle
                } else if (data.get(k).getActivity().equalsIgnoreCase(backupActivity.get(l)) == true
                        && data.get(k).getAbstractState().equalsIgnoreCase("Idle")) {
                    break;
                }
            }// end loop of data
        }
       // printHolder(a);
     
        return a;
    }

    public ArrayList<Eventlog> filter_data(ArrayList<Eventlog> l, String first_act, String second_act) {
        Eventlog e = new Eventlog();
        ArrayList<Eventlog> input = new ArrayList<Eventlog>();

        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getActivity().equals(first_act) == true || l.get(i).getActivity().equals(second_act) == true) {
                e.setActivity(l.get(i).getActivity());
                e.setCase_id(l.get(i).getCase_id());
                e.setEventType(l.get(i).getEventType());
                e.setAbstractState(l.get(i).getAbstractState());
                e.setResources(l.get(i).getResources());
                e.setTime(l.get(i).getTime());
                e.setEvent_id(l.get(i).getEvent_id());
                input.add(e);
                e = new Eventlog();
            }
        }
        //print(input);
        return input;

    }

    public void suspendLowPriorityItems(ArrayList<Eventlog> data, String firstAct, String secondAct) throws SQLException {
        Timestamp t = null;
        
        if (data.isEmpty() == false) {
            
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getActivity().equals(firstAct) == true
                        && data.get(i).getAbstractState().equalsIgnoreCase("Idle") == true
                        && data.get(i).getEventType().equalsIgnoreCase("Complete") == true) {
                    for (int j = i - 1 ; j < data.size(); j--) {
                        if (data.get(j).getEventType().equalsIgnoreCase("Complete") == true && data.get(j).getAbstractState().equalsIgnoreCase("Idle") == true
                                && data.get(j).getActivity().equals(secondAct) == true) {
                            if (data.get(j - 1).getActivity().equalsIgnoreCase(secondAct) == true && data.get(j-1).getEventType().equalsIgnoreCase("Start") == true ) {
                                if (data.get(j - 1).getTime().equals(data.get(j - 2).getTime()) == true) {
                                    max++;
                                    t = new Timestamp(data.get(j - 1).getTime().getTime() + (1 * 1000));
                                    insertDB(max, 1, data.get(j).getCase_id(), data.get(j).getResources(), firstAct, "Suspend", t, "Idle"); 
                                    max++;
                                    Timestamp t1 = new Timestamp(data.get(j).getTime().getTime() + (1 * 1000));
                                    insertDB(max, 1, data.get(j).getCase_id(), data.get(j).getResources(), firstAct, "Start", t1, "Active"); 
                                    break;
                                } else {
                                    max++;
                                    t = new Timestamp(data.get(j - 1).getTime().getTime() - (1 * 1000));
                                    insertDB(max, 1, data.get(j).getCase_id(), data.get(j).getResources(), firstAct, "Suspend", t, "Idle"); 
                                    max++;
                                    Timestamp t1 = new Timestamp(data.get(j).getTime().getTime() + (1 * 1000));
                                    insertDB(max, 1, data.get(j).getCase_id(), data.get(j).getResources(), firstAct, "Start", t1, "Active");
                                    break;
                                }
                            }
                            
                        }else if(data.get(j).getActivity().equalsIgnoreCase(secondAct) == false ){
                            break;
                        } 
                        
                    }

                }
            }
        }

    }

    public void deferLowPriorityItems(ArrayList<Eventlog> data) throws SQLException {
        for (int i = 0; i < data.size() - 1; i++) {
            if (data.get(i).getAbstractState().equalsIgnoreCase("Active") == true && (data.get(i + 1).getAbstractState().equalsIgnoreCase("Active") == true || (data.get(i + 1).getAbstractState().equalsIgnoreCase("Idle") == true && data.get(i + 1).getEventType().equalsIgnoreCase("Allocate") == true))) {
                for (int j = i; j < data.size() - 1; j++) {
                    if (data.get(j).getAbstractState().equalsIgnoreCase("Idle") == true) {
                        if (data.get(j).getActivity().equals(data.get(j - 1).getActivity()) == false) {
                            updateDB(data.get(j - 1).getEvent_id(), "Allocate", "Idle");
                            max++;
                            Timestamp t1 = new Timestamp(data.get(j).getTime().getTime() + (1 * 1000));
                            insertDB(max, 1, data.get(j - 1).getCase_id(), data.get(j - 1).getResources(), data.get(j - 1).getActivity(), "Start", t1, "Active"); // suspend
                            break;
                        }
                    }
                }
                break;
            }

        }
    }

    public void closeOldItem(ArrayList<Eventlog> data) throws SQLException {
        String act = data.get(0).getActivity();
        for (int i = 0; i < data.size()-1; i++) {
            if (data.get(i).getActivity().equalsIgnoreCase(data.get(i+1).getActivity()) == false 
             && data.get(i).getAbstractState().equalsIgnoreCase("Active") == true 
             && data.get(i+1).getAbstractState().equalsIgnoreCase("Active") == true) {
                for(int j = i+1 ; j < data.size() ;j++){
                    if(data.get(j).getActivity().equalsIgnoreCase(data.get(i).getActivity()) == true && data.get(j).getAbstractState().equalsIgnoreCase("Idle") == true)
                    {
                                deleteC(data.get(j).getEvent_id());
                                break;
                    }
                }
                max++;
                Timestamp t1 = new Timestamp(data.get(i+1).getTime().getTime() - (1 * 1000));
                insertDB(max, 1, data.get(i).getCase_id(), data.get(i).getResources(), data.get(i).getActivity(), "Complete", t1, "Idle"); // suspend
            } 
        }
    }

    public void insertDB(int eid, int pid, int cid, String res, String act, String state, Timestamp time, String absState) throws SQLException {
        String insert = "insert into eventlog values(?,?,?,?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(insert);
        ps.setInt(1, eid); // event id
        ps.setInt(2, pid); // process id
        ps.setInt(3, cid); // case id
        ps.setString(4, res); // resource
        ps.setString(5, act); // activity
        ps.setString(6, state); // event type
        ps.setTimestamp(7, time); // timestamp
        ps.setString(8, absState);
        ps.executeUpdate();
    }

    public void deleteC(int index) throws SQLException {

        String delete = "delete from eventlog where event_id = ?";
        PreparedStatement ps = conn.prepareStatement(delete);
        ps.setInt(1, index);
        ps.executeUpdate();

    }


    public void updateDB(int eventid, String et, String abset) throws SQLException {
        String update = " update eventlog set eventtype = ? , absState = ? where event_id = ? ";
        //System.out.println(et + "  " + abset + " " + eventid);
        PreparedStatement ps = conn.prepareStatement(update);
        ps.setString(1, et);
        ps.setString(2, abset);
        ps.setInt(3, eventid);
        ps.executeUpdate();
    }

    public void print(ArrayList<Eventlog> d) {
        for (int k = 0; k < d.size(); k++) {
            System.out.println(d.get(k).getActivity() + "  " + d.get(k).getCase_id() + "  " + d.get(k).getAbstractState() + "  " + d.get(k).getResources() + "  " + d.get(k).getTime() + " " + d.get(k).getEvent_id());
        }
    }

    public void printHolder(ArrayList<Holder> d) {
        for (int k = 0; k < d.size(); k++) {
            System.out.println(d.get(k).getFirst_activity() + "  " + d.get(k).getSecond_activity() + "  " + d.get(k).getIntervals() + "  " + d.get(k).getTime());
        }
    }

    public int maxEvent() throws SQLException {
        int max_event = 0;
        String maximumEvent = "select max(event_id) from eventlog";
        PreparedStatement pS = conn.prepareStatement(maximumEvent);
        ResultSet r = pS.executeQuery();
        while (r.next()) {
            max_event = r.getInt(1);
        }
        return max_event;
    }

}
