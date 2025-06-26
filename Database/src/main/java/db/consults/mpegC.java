package db.consults;

import javax.sql.rowset.*; 
import db.connManager.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class mpegC {
    
    public static long getMaxID(){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select MAX(id) from MPEGFile";
            PreparedStatement statement = connection.prepareStatement(query);    
            ResultSet rs = statement.executeQuery();
            long ret = 1;
            if(rs.next()) ret = rs.getLong("MAX(id)");
            if(ret < 1) ret = 1;
            return ret;
        } catch (SQLException e){
            System.err.println("fail"+e.getMessage());
            return 1;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
        
    public static long getMPEGFile(long id){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select id from MPEGFile where id = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            long retID = -1;
            if(rs.next()) retID = rs.getLong("id");
            return retID;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return -1;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
        
    }
    
    public static boolean exists(long id){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select id from MPEGFile where id = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setLong(1, id);
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
    
    public static String getMPEGFilePath(long id){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select * from MPEGFile where id = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                String path = rs.getString("path");
                ConnectionManager.close_connection(connection);
                return path;
            }
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return null;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
        return null;
    }
    
    public static boolean hasDG(long id) {
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select dg_id from DatasetGroup where mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setLong(1, id);
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
    
    public static ResultSet getDG(long id){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select dg_id from DatasetGroup where mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setLong(1, id);
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
    
    public static String getString(long id, String field){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select "+field+" from MPEGFile where id = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            String retField = null;
            if(rs.next()) retField = rs.getString(field);
            return retField;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return null;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    public static ResultSet getOwnFiles(String owner){
    Connection connection = null;
    try {
        connection = ConnectionManager.init_connection();
        String query = "select id from MPEGFile where owner = ?";
        PreparedStatement statement = connection.prepareStatement(query);    
        statement.setString(1, owner);
        ResultSet rs = statement.executeQuery();
        CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
        crs.populate(rs);
        return crs;
    } catch (SQLException e){
        System.err.println(e.getMessage());
        return null;
    } finally {
        if (connection != null) {
            ConnectionManager.close_connection(connection);
        }
    }
}

    
}