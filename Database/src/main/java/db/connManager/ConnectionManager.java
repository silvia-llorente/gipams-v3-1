package db.connManager;

import java.io.FileInputStream;
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
            prop.load(ConnectionManager.class.getClassLoader().getResourceAsStream("app.properties"));
            //prop.load(new FileInputStream("/home/alumne/NetBeansProjects/Database/src/main/java/db/connManager/app.properties")); //getContextPath
            driver = prop.getProperty("db.driver");
            username = prop.getProperty("db.username");
            password = prop.getProperty("db.password");
            urlDB = prop.getProperty("db.url");
            System.out.println(driver + " " + username+ " " + password+ " " + urlDB);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, "Properties file for db connection not found! ERROR", ex);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, "Could not read properties file for BD connection! ERROR", ex);
        }
        try {
            
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            connection = DriverManager.getConnection(urlDB+"?autoReconnect=true&useSSL=false",username,password);
            //System.out.println("Connection: " + connection);
            return connection;
        } catch (SQLException e) {
            System.err.println("CONNECTION ERROR!\n\n"+e.getMessage());
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
            // connection close failed.
            System.err.println(e.getMessage());
        }
    }
}
