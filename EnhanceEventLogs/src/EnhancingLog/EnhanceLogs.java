/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EnhancingLog;

import static EnhancingLog.ConnDB.conn;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.SAXException;

/**
 *
 * @author nesma
 */
public class EnhanceLogs {
     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException, InstantiationException, IOException, InvalidFormatException, ParserConfigurationException, SAXException, ParseException {
        /* connect to database */
        ConnDB c = new ConnDB();
        c.connectDB();
        
        /* parse file in any format XES or Excel */
        Parse p = new Parse();
        p.ParseEXCEL("RData.xlsx", conn);
        //p.parseXES("x.xes", conn);
        
        /* Enhance Logs */
        ConcurrentTasks conc = new ConcurrentTasks();
        conc.CT();
    }
}
