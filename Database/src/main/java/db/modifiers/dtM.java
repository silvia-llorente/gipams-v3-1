package db.modifiers;

import db.connManager.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class dtM {
    public static boolean insertDT(int dt_id, String title, String type, String abs, String projC, String desc, Boolean hasM, String protection, String path, String owner, Integer dg_id, Long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try {       
            PreparedStatement statement;
            String query = "INSERT INTO Dataset (dt_id, title, type, abstract, projectCentre, description, path, owner, hasMetadata, protection, dg_id, mpegfile) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(query);
            statement.setInt(1, dt_id);
            statement.setString(2, title);
            statement.setString(3, type);
            statement.setString(4, abs);
            statement.setString(5, projC);
            statement.setString(6, desc);
            statement.setString(7, path);
            statement.setString(8, owner);
            statement.setBoolean(9, hasM);
            statement.setString(10, protection);
            statement.setInt(11, dg_id);
            statement.setLong(12, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean deleteDT(int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{       
            PreparedStatement statement;
            String query = "DELETE FROM Dataset WHERE dt_id = ? AND dg_id = ? AND mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setInt(1,dt_id);
            statement.setInt(2,dg_id);
            statement.setLong(3,mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean updateDT(int dt_id, String title, String type, String abs, String projC, String desc, Boolean hasM, String protection, String path, String owner, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        String values = "";
        boolean first = true;
        if(dt_id != -1){values+="dt_id = ?"; first = false;}
        if(title != null){if(first){values+="title = ?";first=false;}else values+=", title = ?";}
        if(type != null){if(first){values+="type = ?";first=false;}else values+=", type = ?";}
        if(abs != null){if(first){values+="abstract = ?";first=false;}else values+=", abstract = ?";}
        if(projC != null){if(first){values+="projectCentre = ?";first=false;}else values+=", projectCentre = ?";}
        if(desc != null){if(first)values+="description = ?";else values+=", description = ?";}
        values += ", hasMetadata = ?, protection = ?";
        try {       
            PreparedStatement statement;
            String query = "UPDATE Dataset SET " + values + " WHERE dt_id = ? AND dg_id = ? AND mpegfile = ?";
            statement = connection.prepareStatement(query);
            int count = 1;
            if(dt_id != -1) {statement.setInt(count, dg_id);count++;}
            if(title != null) {statement.setString(count, title);count++;}
            if(type != null) {statement.setString(count, type);count++;}
            if(abs != null) {statement.setString(count, abs);count++;}
            if(projC != null) {statement.setString(count, projC);count++;}
            if(desc != null) {statement.setString(count, desc);count++;}
            statement.setBoolean(count, hasM);count++;
            statement.setString(count, protection);count++;
            statement.setInt(count, dt_id); count++;
            statement.setInt(count, dg_id); count++;
            statement.setLong(count, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }    
    
    public static boolean updatePR(long mpegfile, int dg_id, int dt_id, String pr){
        try{    
            Connection connection = ConnectionManager.init_connection();
            PreparedStatement statement;
            String query = "UPDATE Dataset set protection = ? where dt_id = ? and dg_id = ? and mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setString(1,pr);
            statement.setInt(2, dt_id);
            statement.setInt(3,dg_id);
            statement.setLong(4, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}