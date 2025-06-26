package db.consults;

import db.connManager.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.*;

public class dtC {
    
    public static boolean exists(int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select dt_id from Dataset where dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, dt_id);
            statement.setInt(2, dg_id);
            statement.setLong(3, mpegfile);
            ResultSet rs = statement.executeQuery();
            boolean exists = false;
            if(rs.next()) exists = true;
             
            return exists;
        } catch (SQLException e){
            System.err.println(e.getMessage());
             
            return false;
        }
    }   
    
    public static String getDTPath(int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select path from Dataset where dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, dt_id);
            statement.setInt(2, dg_id);
            statement.setLong(3, mpegfile);
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
    
    public static int getMaxID(int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select MAX(dt_id) from Dataset where dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, dg_id);
            statement.setLong(2, mpegfile);
            ResultSet rs = statement.executeQuery();
            int ret = 1;
            if(rs.next()) ret = rs.getInt("MAX(dt_id)"); 
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
    
    public static boolean hasProtection(int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select protection from Dataset where dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, dt_id);
            statement.setInt(2, dg_id);
            statement.setLong(3, mpegfile);
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
    
    public static String getString(int dt_id, String field, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select "+field+" from Dataset where dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, dt_id);
            statement.setInt(2, dg_id);
            statement.setLong(3, mpegfile);
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
    
    public static ResultSet getOwnDT(String owner){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select dt_id from Dataset where owner = ?";
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
    
    public static ResultSet findByMetadata(String center, String description, String title, String type, String taxon_id){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select dt_id,dg_id,mpegfile from Dataset where";
            String operator = "AND";
            Boolean first = true;
            String op;
            List <String> params = new ArrayList<>();
            if (center != null && !center.equals("")) {
                query = query.concat(" center LIKE ?");                
                first = false;
                params.add(center);
            }
            if (description != null && !description.equals("")) {
                op = (first) ? "" : operator; // fiquem AND o OR si abans ja hem ficat algun altre atribut
                query = query.concat(op+" description LIKE ?");                
                first = false;
                params.add(description);
            }
            if (title != null && !title.equals("")) {
                op = (first) ? "" : operator; // fiquem AND o OR si abans ja hem ficat algun altre atribut
                query = query.concat(op+" title LIKE ?");                
                first = false;
                params.add(title);
            }
            if (type != null && !type.equals("")) {
                op = (first) ? "" : operator; // fiquem AND o OR si abans ja hem ficat algun altre atribut
                query = query.concat(op+" type LIKE ?");                
                params.add(type);
            }

            if (params.isEmpty()) {
                query = "SELECT * FROM Dataset"; // Aquí hacemos un SELECT completo
            }
            
            PreparedStatement statement = connection.prepareStatement(query);
            for (int w = 0; w < params.size(); w++) statement.setString(w+1, "%"+params.get(w)+"%");
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
    
    public static String getPR(long mpegfile, int dg_id, int dt_id){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select protection from Dataset where dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, dt_id);
            statement.setInt(2, dg_id);
            statement.setLong(3, mpegfile);
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
    
    public static boolean hasAU(int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select au_id from AccessUnit where dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, dt_id);
            statement.setInt(2, dg_id);
            statement.setLong(3, mpegfile);
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
    
    public static ResultSet getAU(int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select au_id from AccessUnit where dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, dt_id);
            statement.setInt(2, dg_id);
            statement.setLong(3, mpegfile);
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
   
}
