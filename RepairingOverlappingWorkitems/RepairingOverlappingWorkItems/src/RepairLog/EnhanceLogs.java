/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RepairLog;

import static RepairLog.ConnDB.conn;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.StringTokenizer;
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
        p.ParseEXCEL("LOG2.xlsx", conn);
        

        /* Enhance Logs */
        RepairingLogs conc = new RepairingLogs();
        // mapping event type to abstract states
        conc.mapping();

		// Write in first strategy (COF(indicate close order item) or DL ( indicate Defer low priority))
        System.out.println("Enter your first strategy: ");
        BufferedReader first_strategy = new BufferedReader(new InputStreamReader(System.in));
        String firstStrategy = first_strategy.readLine();
		// Write in first strategy (COF(indicate close order item) or SL ( indicate Suspend low priority))
        System.out.println("Enter your second strategy: ");
        BufferedReader second_strategy = new BufferedReader(new InputStreamReader(System.in));
        String secondStrategy = second_strategy.readLine();
        conc.CT(firstStrategy, secondStrategy);

    }
}
