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
package EnhancingLog;

import static EnhancingLog.ConnDB.conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
public class ConcurrentTasks {

    int max = 0;

    public void CT() throws SQLException, ClassNotFoundException, InstantiationException {

        ConnDB c = new ConnDB();
        c.connectDB();
        Eventlog e = new Eventlog();
        ArrayList<String> Cases = new ArrayList<String>();
        ArrayList<String> resources = new ArrayList<String>();
        ArrayList<Eventlog> data = new ArrayList<Eventlog>();
        max = maxEvent();
        String res = "select distinct resources from eventlog";
        PreparedStatement preStat = conn.prepareStatement(res);
        ResultSet rs1 = preStat.executeQuery();
        while (rs1.next()) {
            resources.add(rs1.getString(1));
        }
        for (int j = 0; j < resources.size(); j++) {
            data.clear();
            String detail = "select case_id,activity,eventtype,time_stamp,event_id,resources from eventlog where resources = ? order by time_stamp";
            PreparedStatement pre = conn.prepareStatement(detail);
            pre.setString(1, resources.get(j));
            ResultSet rs2 = pre.executeQuery();
            while (rs2.next()) {
                e.setActivity(rs2.getString(2));
                e.setCase_id(rs2.getInt(1));
                e.setEventType(rs2.getString(3));
                e.setEvent_id(rs2.getInt(5));
                e.setResources(rs2.getString(6));
                e.setTime(rs2.getTimestamp(4));
                data.add(e);
                e = new Eventlog();
            }
            preProcessing(data);
        }
    }

    boolean flag = true;

    public void preProcessing(ArrayList<Eventlog> data) throws SQLException {
        int index = 0;
        Eventlog e = new Eventlog();
        Timestamp t = null;
        boolean insert = false;

        for (int k = 0; k < data.size() - 1; k++) {
            for (int h = k + 1; h < data.size(); h++) {
                insert = false;

                if (data.get(k).getActivity().equals(data.get(h).getActivity()) == false
                        && (data.get(k).getEventType().equals("START") == true
                        && data.get(h).getEventType().equals("START") == true)) {
                    for (int l = h; l < data.size(); l++) {
                        if (data.get(l).getEventType().equals("COMPLETE") == true && data.get(k).getEventType().equals("START") == true) {
                            System.out.println(" activity " + data.get(k).getActivity() + " second l " + data.get(l).getActivity());
                            if (data.get(l).getActivity().equals(data.get(k).getActivity()) == false && data.get(l).getActivity().equals(data.get(h).getActivity()) == false && insert == false) {
                                max++;
                                insertDB(max, 1, data.get(k).getCase_id(), data.get(k).getResources(), data.get(k).getActivity(), "SUSPEND", data.get(h).getTime());
                                insert = true;
                            }
                            if (data.get(l).getActivity().equals(data.get(k).getActivity()) == false && insert == false) {
                                max++;
                                insertDB(max, 1, data.get(k).getCase_id(), data.get(k).getResources(), data.get(k).getActivity(), "SUSPEND", data.get(h).getTime());
                                insert = true;
                            }
                            if (data.get(l).getActivity().equals(data.get(k).getActivity()) == true) {
                                backTrack(data, l);
                                break;
                            }
                        }
                    }
                    break;
                } else if (data.get(k).getActivity().equals(data.get(h).getActivity()) == false && data.get(k).getEventType().equals("START") == true
                        && data.get(h).getEventType().equals("COMPLETE") == true) {
                    boolean f = SearchET(data, k, h);
                    if (f == false) {
                        if (data.get(k).getCase_id() == data.get(h).getCase_id()) {
                            String r = Relations(data.get(k).getActivity(), data.get(h).getActivity());
                            if (r.equalsIgnoreCase("Interleaving")) {
                                t = searchDB(data.get(k).getCase_id(), data.get(h).getActivity(), "START");
                                if (t != null) {
                                    max++;
                                    insertDB(max, 1, data.get(k).getCase_id(), data.get(k).getResources(), data.get(k).getActivity(), "SUSPEND", t);
                                    max++;
                                    insertDB(max, 1, data.get(h).getCase_id(), data.get(h).getResources(), data.get(h).getActivity(), "START", t);
                                }
                            } else if (r.equalsIgnoreCase("Execlusive") || r.equalsIgnoreCase("Order")) {
                                t = searchDB(data.get(h).getCase_id(), data.get(k).getActivity(), "COMPLETE");
                                if (t != null) {
                                    max++;
                                    if (t.before(data.get(h).getTime()) == true) {
                                        insertDB(max, 1, data.get(h).getCase_id(), data.get(h).getResources(), data.get(h).getActivity(), "START", t);
                                    }
                                    max++;
                                    t = searchDB(data.get(k).getCase_id(), data.get(k).getActivity(), "START");
                                    if (t.after(data.get(k).getTime()) == true) {
                                        insertDB(max, 1, data.get(k).getCase_id(), data.get(k).getResources(), data.get(k).getActivity(), "SUSPEND", t);
                                    }
                                }
                            }
                        }
                        break;
                    }
                } else if (data.get(k).getActivity().equals(data.get(h).getActivity()) == false && data.get(k).getEventType().equals("COMPLETE") == true
                        && data.get(h).getEventType().equals("COMPLETE") == true && flag == true) {
                    max++;
                    insertDB(max, 1, data.get(h).getCase_id(), data.get(h).getResources(), data.get(h).getActivity(), "START", data.get(k).getTime());
                    break;
                } else if (data.get(k).getActivity().equals(data.get(h).getActivity()) == false && data.get(k).getEventType().equals("COMPLETE") == true
                        && data.get(h).getEventType().equals("SUSPEND") == true) {
                    max++;
                    insertDB(max, 1, data.get(h).getCase_id(), data.get(h).getResources(), data.get(h).getActivity(), "START", data.get(k).getTime());
                    break;
                } else if (data.get(k).getActivity().equals(data.get(h).getActivity()) == false && data.get(k).getEventType().equals("SUSPEND") == true
                        && data.get(h).getEventType().equals("COMPLETE") == true) {
                    max++;
                    insertDB(max, 1, data.get(h).getCase_id(), data.get(h).getResources(), data.get(h).getActivity(), "START", data.get(k).getTime());
                    break;
                } else if (data.get(k).getActivity().equals(data.get(h).getActivity()) == false && data.get(k).getEventType().equals("SUSPEND") == true
                        && data.get(h).getEventType().equals("SUSPEND") == true) {
                    max++;
                    insertDB(max, 1, data.get(h).getCase_id(), data.get(h).getResources(), data.get(h).getActivity(), "START", data.get(k).getTime());
                    break;
                } else if (data.get(k).getActivity().equals(data.get(h).getActivity()) == false && data.get(k).getEventType().equals("COMPLETE") == true
                        && data.get(h).getEventType().equals("START") == true) {
                    break;
                } else {
                    break;
                }
            }
        }
        // print(data);
    }

    public void backTrack(ArrayList<Eventlog> e, int currentIndex) throws SQLException {
        Eventlog e1 = new Eventlog();
        Timestamp t = null;
        for (int b = currentIndex - 1; b < e.size(); b--) {
            if (b == 0) {
                break;
            }
            if (e.get(b).getActivity().equals(e.get(currentIndex).getActivity()) == true
                    && e.get(b).getEventType().equals("START") == true
                    && e.get(b).getCase_id() == e.get(currentIndex).getCase_id()) {
                break;
            }
            if (e.get(b).getActivity().equals(e.get(currentIndex).getActivity()) == false
                    && e.get(b).getEventType().equals("SUSPEND")) {
                max++;
                insertDB(max, 1, e.get(currentIndex).getCase_id(), e.get(currentIndex).getResources(), e.get(currentIndex).getActivity(), "START", e.get(b).getTime());
                break;
            }
            if (e.get(b).getActivity().equals(e.get(currentIndex).getActivity()) == false
                    && e.get(b).getEventType().equals("START")) {
                e.get(b).setEventType("ALLOCATE");
                updateDB(e.get(b).getEvent_id(), "ALLOCATE");
            }
            if (e.get(b).getActivity().equals(e.get(currentIndex).getActivity()) == true
                    && e.get(b).getEventType().equals("START") == true
                    && e.get(b).getCase_id() == e.get(currentIndex).getCase_id()
                    && e.get(currentIndex).getEventType().equals("COMPLETE") == true) {
                break;
            }
            if (e.get(b).getActivity().equals(e.get(currentIndex).getActivity()) == false
                    && (e.get(b).getEventType().equals("COMPLETE") == true
                    || (e.get(b).getEventType().equals("COMPLETE") == true && e.get(currentIndex).getEventType().equals("COMPLETE") == true))) {
                max++;
                insertDB(max, 1, e.get(currentIndex).getCase_id(), e.get(currentIndex).getResources(), e.get(currentIndex).getActivity(), "START", e.get(b).getTime());
                flag = false;
                break;
            }
        }
    }

    public Timestamp searchDB(int caseid, String activity, String eventtype) throws SQLException {
        Timestamp t = null;
        String select = "select time_stamp from eventlog where case_id = ? and activity = ? and eventtype = ? order by time_stamp desc limit 1";
        PreparedStatement preStat = conn.prepareStatement(select);
        preStat.setInt(1, caseid);
        preStat.setString(2, activity);
        preStat.setString(3, eventtype);
        ResultSet rs1 = preStat.executeQuery();
        while (rs1.next()) {
            t = rs1.getTimestamp(1);
        }
        if (t == null) {
            return null;
        } else {
            return t;
        }
    }

    public boolean SearchET(ArrayList<Eventlog> d, int i, int j) {
        boolean flag = false;
        for (int k = 0; k < d.size(); k++) {
            if (d.get(k).getActivity().equals(d.get(i).getActivity()) == true && d.get(k).getEventType().equals("COMPLETE") == true) {
                flag = true;
                break;
            }
            if (d.get(k).getActivity().equals(d.get(j).getActivity()) == true && d.get(k).getEventType().equals("START") == true) {
                flag = true;
                break;
            } else {
                flag = false;
            }
        }
        return flag;
    }

    public void insertDB(int eid, int processid, int cid, String res, String act, String et, Timestamp time) throws SQLException {
        String insert = "insert into eventlog values(?,?,?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(insert);
        ps.setInt(1, eid); // event id
        ps.setInt(2, processid); // process id
        ps.setInt(3, cid); // case id
        ps.setString(4, res); // resource
        ps.setString(5, act); // activity
        ps.setString(6, et); // event type
        ps.setTimestamp(7, time); // timestamp
        ps.executeUpdate();
    }

    public void deleteDB(int eventid) throws SQLException {

        String delete = "delete from eventlog where event_id = ? ";
        PreparedStatement ps = conn.prepareStatement(delete);
        ps.setInt(1, eventid);
        ps.executeUpdate();
    }

    public void updateDB(int eventid, String et) throws SQLException {
        String update = " update eventlog set eventtype = ? where event_id = ? ";
        PreparedStatement ps = conn.prepareStatement(update);
        ps.setString(1, et);
        ps.setInt(2, eventid);
        ps.executeUpdate();
    }

    public void print(ArrayList<Eventlog> d) {
        for (int k = 0; k < d.size(); k++) {
            System.out.println(d.get(k).getActivity() + "  " + d.get(k).getCase_id() + "  " + d.get(k).getEventType() + "  " + d.get(k).getResources() + "  " + d.get(k).getTime() + " " + d.get(k).getEvent_id());
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

    public String Relations(String x, String y) {
        NetSystem n = createBP();
        BehaviouralProfile<NetSystem, Node> bp1 = BPCreatorNet.getInstance().deriveRelationSet(n);
        Object o1 = x;
        Object o2 = y;
        Transition t1 = null;
        Transition t2 = null;
        ArrayList<Object> arr = new ArrayList<Object>(bp1.getModel().getTransitions());
        for (int i = 0; i < arr.size(); i++) {

            if (arr.get(i).toString().equals(o1)) {

                t1 = (Transition) arr.get(i);
            }
            if (arr.get(i).toString().equals(o2)) {
                t2 = (Transition) arr.get(i);
            }
        }
        RelSetType relation = bp1.getRelationForEntities(t1, t2);
        return relation.name();
    }

    public NetSystem createBP() {
        PetriNet net1 = new PetriNet();

        // Create the transitions with according labels
        Transition a = new Transition("A");
        Transition b = new Transition("B");
        Transition c = new Transition("C");
        Transition d = new Transition("D");

        // Add transitions to net
        net1.addNode(a);
        net1.addNode(b);
        net1.addNode(c);
        net1.addNode(d);

        // Create places
        Place p1 = new Place("1");
        Place p2 = new Place("2");
        Place p3 = new Place("3");
        Place p4 = new Place("4");
        Place p5 = new Place("5");
        Place p6 = new Place("6");
        // Add places to net
        net1.addNode(p1);
        net1.addNode(p2);
        net1.addNode(p3);
        net1.addNode(p4);
        net1.addNode(p5);
        net1.addNode(p6);

        Marking m = new Marking();
        m.setPetriNet(net1);
        m.put(p1, 1);

        // Add control flow edges
        net1.addFlow(p1, a);
        net1.addFlow(a, p2);
        net1.addFlow(a, p3);
        net1.addFlow(p2, b);
        net1.addFlow(p3, c);
        net1.addFlow(b, p4);
        net1.addFlow(c, p5);
        net1.addFlow(p4, d);
        net1.addFlow(p5, d);
        net1.addFlow(d, p6);

        NetSystem n = new NetSystem(net1);
        return n;
    }

}
