package ps.DB;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionManager {
    static String urlDB = null;
    static String username = null;
    static String password = null;
    static String driver = null;
    
    static Connection connection = null;
    
    static public Connection init_connection(){
        Properties prop = new Properties();
        try{
            prop.load(ConnectionManager.class.getClassLoader().getResourceAsStream("app.properties")); //getContextPath
            driver = prop.getProperty("db.driver");
            username = prop.getProperty("db.username");
            password = prop.getProperty("db.password");
            urlDB = prop.getProperty("db.url");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, "PS: Properties file for db connection not found! ERROR", ex);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, "PS: Could not read properties file for BD connection! ERROR", ex);
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(urlDB+"?autoReconnect=true&useSSL=false",username,password);
            return connection;
        } catch (SQLException e) {
            System.err.println("PS: CONNECTION ERROR!\n\n"+e.getMessage());
        } catch (ClassNotFoundException ex) { 
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;   
    }
    
    static public void close_connection(Connection connect){
        try {
            if (connect != null) {
                connect.close();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
