package db.consults;

import db.connManager.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.*;

public class auC {
    
    public static boolean exists(int au_id, int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select dt_id from AccessUnit where au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, au_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();
            boolean exists = false;
            if(rs.next()) exists = true;
            return exists;
        } catch (SQLException e){
            System.err.println(e.getMessage()); 
            return false;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }   
    
    public static String getAUPath(int au_id, int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select path from AccessUnit where au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, au_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();
            String path = null;
            if(rs.next()) path = rs.getString("path");
            return path;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return null;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    public static int getMaxID(int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select MAX(au_id) from AccessUnit where dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, dt_id);
            statement.setInt(2, dg_id);
            statement.setLong(3, mpegfile);
            ResultSet rs = statement.executeQuery();
            int ret = 1;
            if(rs.next()) ret = rs.getInt("MAX(au_id)");
            return ret;
        } catch (SQLException e){
            System.err.println(e.getMessage()); 
            return 1;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    public static boolean hasProtection(int au_id, int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select protection from AccessUnit where au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, au_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();
            String hasP = null;
            if(rs.next()) hasP = rs.getString("protection");
            return !(hasP == null || hasP.isEmpty());
        } catch (SQLException e){
            System.err.println(e.getMessage());  
            return false;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    public static ResultSet getOwnBlocs(String owner){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select au_id from AccessUnit where owner = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setString(1, owner);
            ResultSet rs = statement.executeQuery();
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            return crs;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return null;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    public static String getPR(long mpegfile, int dg_id, int dt_id, int au_id){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select protection from AccessUnit where au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, au_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();
            String hasP = null;
            if(rs.next()) hasP = rs.getString("protection");
            return hasP;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return null;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    public static boolean hasBlocks(int au_id, int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select block_id from Block where au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, au_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();
            boolean exists = false;
            if(rs.next()) exists = true;
            return exists;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return false;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    public static ResultSet getBlocks(int au_id, int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select block_id from Block where au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, au_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            return crs;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return null;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    
    public static boolean blockExists(int block_id, int au_id, int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select block_id from Block where block_id = ? AND au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, block_id);
            statement.setInt(2, au_id);
            statement.setInt(3, dt_id);
            statement.setInt(4, dg_id);
            statement.setLong(5, mpegfile);
            ResultSet rs = statement.executeQuery();
            boolean exists = false;
            if(rs.next()) exists = true;
            return exists;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return false;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    public static String getBlockPath(int block_id, int au_id, int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select path from Block where block_id = ? AND au_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, block_id);
            statement.setInt(2, au_id);
            statement.setInt(3, dt_id);
            statement.setInt(4, dg_id);
            statement.setLong(5, mpegfile);
            ResultSet rs = statement.executeQuery();
            String path = null;
            if(rs.next()) path = rs.getString("path");
            return path;
        } catch (SQLException e){
            System.err.println(e.getMessage());

            return null;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
}
