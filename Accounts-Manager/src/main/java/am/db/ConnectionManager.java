package am.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionManager {

    static public Connection init_connection() {
        String urlDB;
        String username;
        String password;
        try {

            Properties props = new Properties();
            props.load(ConnectionManager.class.getClassLoader().getResourceAsStream("app.properties"));

            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            urlDB = props.getProperty("db.url");
            String driver = props.getProperty("db.driver");
            //System.out.println(driver + " " + username + " " + password + " " + urlDB);

            Class.forName(driver);
            System.out.println("BEGINNING CONNECTION");
            Connection connection = DriverManager.getConnection(urlDB + "?autoReconnect=true", username, password); // &useSSL=false
            System.out.println("ENDING CONNECTION");
            return connection;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(db.connManager.ConnectionManager.class.getName()).log(Level.SEVERE, "Properties file for db connection not found! ERROR", ex);
        } catch (IOException ex) {
            Logger.getLogger(db.connManager.ConnectionManager.class.getName()).log(Level.SEVERE, "Could not read properties file for BD connection! ERROR", ex);
        } catch (SQLException e) {
            System.out.println("CONNECTION ERROR!\n\n" + e.getMessage());
        } catch (ClassNotFoundException e) {
            Logger.getLogger(db.connManager.ConnectionManager.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;


    }

    static public void close_connection(Connection connect) {
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
