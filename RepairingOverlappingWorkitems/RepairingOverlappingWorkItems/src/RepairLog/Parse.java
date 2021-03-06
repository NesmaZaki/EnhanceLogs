/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RepairLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author nesma
 */
public class Parse {

    public void parseXES(String filename, Connection connect) throws ParserConfigurationException, SAXException, IOException, ParseException, SQLException {
        File fXmlFile = new File(filename);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        Element docEle = doc.getDocumentElement();
        System.out.println(docEle.getNodeName());
        String id = "";
        String eventtype = "";
        String activity = "";
        String resource = "";
        String timestamp = "";
        String insert = "insert into eventlog (resources,activity,eventtype,time_stamp,case_id,event_id,process_id) values  (?,?,?,?,?,?,?)";
        PreparedStatement insertRaw = null;
        insertRaw = connect.prepareStatement(insert);
        int count = 1;
        java.sql.Timestamp Timestamp = null;
        NodeList nl = docEle.getElementsByTagName("trace");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n1 = nl.item(i);
                Element e = (Element) n1;
                NodeList n = n1.getChildNodes();
                for (int l1 = 0; l1 < n.getLength(); l1++) {
                    if (n.item(l1).getNodeType() == Node.ELEMENT_NODE) {
                        Node pro1 = n.item(l1);
                        if (pro1.getNodeName().equals("event") == true) {
                            NodeList childs = pro1.getChildNodes();
                            for (int l = 0; l < childs.getLength(); l++) {
                                if (childs.item(l).getNodeType() == Node.ELEMENT_NODE) {
                                    Node pro = childs.item(l);
                                    String key = pro.getAttributes().getNamedItem("key").toString();
                                    if (key.contains("name")) {
                                        activity = pro.getAttributes().getNamedItem("value").getNodeValue();
                                    }
                                    if (key.contains("transition")) {
                                        eventtype = pro.getAttributes().getNamedItem("value").getNodeValue();
                                    }
                                    if (key.contains("resource")) {
                                        resource = pro.getAttributes().getNamedItem("value").getNodeValue();
                                    }
                                    if (key.contains("instance")) {
                                        id = pro.getAttributes().getNamedItem("value").getNodeValue();
                                    }
                                    if (key.contains("timestamp")) {
                                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        String time = pro.getAttributes().getNamedItem("value").getNodeValue();
                                        timestamp = time.replace("T", " ");
                                        Date date = dateFormat.parse(timestamp);
                                        long timest = date.getTime();
                                        Timestamp = new java.sql.Timestamp(timest);
                                    }
                                }

                            }
                            if (resource == null) {
                                resource = "Dummy";
                            }
                            insertRaw.setString(1, resource);
                            insertRaw.setString(2, activity);
                            insertRaw.setString(3, eventtype);
                            insertRaw.setTimestamp(4, Timestamp);
                            insertRaw.setString(5, id);
                            insertRaw.setInt(6, count);
                            insertRaw.setString(7, "1");
                            insertRaw.executeUpdate();
                            count++;
                            resource = null;
                        } else {
                            String key1 = pro1.getAttributes().getNamedItem("key").toString();
                            if (key1.contains("name")) {
                                id = pro1.getAttributes().getNamedItem("value").getNodeValue();
                            }

                        }
                    }
                }
            }// end loop of trace
        }
    }

    public void ParseEXCEL(String FileName, Connection connect) throws IOException, InvalidFormatException, SQLException {

        System.out.print("Parsing");
        InputStream file = new FileInputStream(FileName);
        Workbook wb = WorkbookFactory.create(file);
        Sheet sheet = wb.getSheetAt(0);
        String insert = "insert into eventlog values (?,1,?,?,?,?,?,?)";
        System.out.println(sheet.getFirstRowNum()+1);
        PreparedStatement p = connect.prepareCall(insert);
        for (int i = sheet.getFirstRowNum()+1; i < sheet.getLastRowNum(); i++) {
            String Request = sheet.getRow(i).getCell(3).toString();
            String EmpName = sheet.getRow(i).getCell(2).toString();
            String EventType = sheet.getRow(i).getCell(4).toString();
            String case_id = sheet.getRow(i).getCell(1).toString();
            //int c = Integer.parseInt(case_id);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date time = sheet.getRow(i).getCell(5).getDateCellValue();
            long timest = time.getTime();
            java.sql.Timestamp Timestamp = new java.sql.Timestamp(timest);
            //int id = Integer.parseInt(Request);
            p.setInt(1, i);
            p.setString(3, EmpName);
            p.setString(2, case_id);
            p.setString(4, Request);
            p.setString(5, EventType);
            p.setTimestamp(6, Timestamp);
            p.setString(7,null);
            p.executeUpdate();
        }
    }
}
