/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EnhancingLog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nesma
 */
public class ConnDB {
    static Connection conn;
     public void connectDB() throws ClassNotFoundException, InstantiationException, SQLException {
        String url = "jdbc:mysql://localhost:3306/concurrenttasklog";
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ConnDB.class.getName()).log(Level.SEVERE, null, ex);
        }
       conn = (com.mysql.jdbc.Connection) DriverManager.getConnection(url, "root", "1234");
    }
}
