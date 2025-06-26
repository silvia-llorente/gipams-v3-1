package db.modifiers;

import db.connManager.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class auM {
    public static boolean insertAU(int au_id, String type, String protection, String path, String owner, int dt_id, Integer dg_id, Long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try {       
            PreparedStatement statement;
            String query = "INSERT INTO AccessUnit (au_id, type, path, owner, protection, dt_id, dg_id, mpegfile) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(query);
            statement.setInt(1, au_id);
            statement.setString(2, type);
            statement.setString(3, path);
            statement.setString(4, owner);
            statement.setString(5, protection);
            statement.setInt(6, dt_id);
            statement.setInt(7, dg_id);
            statement.setLong(8, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean deleteAU(int au_id, int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{       
            PreparedStatement statement;
            String query = "DELETE FROM AccessUnit WHERE au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setInt(1,au_id);
            statement.setInt(2,dt_id);
            statement.setInt(3,dg_id);
            statement.setLong(4,mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean insertBlock(int block_id, int size, String path, String owner, int au_id, int dt_id, Integer dg_id, Long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try {       
            PreparedStatement statement;
            String query = "INSERT INTO Block (block_id, size, path, owner, au_id, dt_id, dg_id, mpegfile) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(query);
            statement.setInt(1, block_id);
            statement.setInt(2, size);
            statement.setString(3, path);
            statement.setString(4, owner);
            statement.setInt(5, au_id);
            statement.setInt(6, dt_id);
            statement.setInt(7, dg_id);
            statement.setLong(8, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean deleteBlock(int block_id, int au_id, int dt_id, Integer dg_id, Long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{       
            PreparedStatement statement;
            String query = "DELETE FROM Block WHERE block_id = ? AND au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setInt(1,block_id);
            statement.setInt(2,au_id);
            statement.setInt(3,dt_id);
            statement.setInt(4,dg_id);
            statement.setLong(5,mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean updatePR(long mpegfile, int dg_id, int dt_id, int au_id, String pr){
        try{    
            Connection connection = ConnectionManager.init_connection();
            PreparedStatement statement;
            String query = "UPDATE AccessUnit set protection = ? where au_id = ? and dt_id = ? and dg_id = ? and mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setString(1,pr);
            statement.setInt(2, au_id);
            statement.setInt(3, dt_id);
            statement.setInt(4,dg_id);
            statement.setLong(5, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}