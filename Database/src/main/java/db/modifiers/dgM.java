package db.modifiers;

import db.connManager.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class dgM { 
    public static boolean insertDG(int dg_id, String title, String type, String abs, String projC, String desc, Boolean hasM, String protection, String path, String owner, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try {       
            PreparedStatement statement;
            String query = "INSERT INTO DatasetGroup (dg_id, title, type, abstract, projectCentre, description, path, owner, hasMetadata, protection, mpegfile) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(query);
            statement.setInt(1, dg_id);
            statement.setString(2, title);
            statement.setString(3, type);
            statement.setString(4, abs);
            statement.setString(5, projC);
            statement.setString(6, desc);
            statement.setString(7, path);
            statement.setString(8, owner);
            statement.setBoolean(9, hasM);
            statement.setString(10, protection);
            statement.setLong(11, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean deleteDG(int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{       
            PreparedStatement statement;
            String query = "DELETE FROM DatasetGroup WHERE dg_id = ? AND mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setInt(1,dg_id);
            statement.setLong(2,mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        } 
    }
    
    public static boolean updateDG(int dg_id, String title, String type, String abs, String projC, String desc, Boolean hasM, String protection, String path, String owner, Long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        String values = "";
        boolean first = true;
        if(dg_id != -1){values+="dg_id = ?"; first = false;}
        if(title != null){if(first){values+="title = ?";first=false;}else values+=", title = ?";}
        if(type != null){if(first){values+="type = ?";first=false;}else values+=", type = ?";}
        if(abs != null){if(first){values+="abstract = ?";first=false;}else values+=", abstract = ?";}
        if(projC != null){if(first){values+="projectCentre = ?";first=false;}else values+=", projectCentre = ?";}
        if(desc != null){if(first)values+="description = ?";else values+=", description = ?";}
        values += ", hasMetadata = ?, protection = ?";
        try {       
            PreparedStatement statement;
            String query = "UPDATE DatasetGroup SET " + values + " WHERE dg_id = ? AND mpegfile = ?";
            statement = connection.prepareStatement(query);
            int count = 1;
            if(dg_id != -1) {statement.setInt(count, dg_id);count++;}
            if(title != null) {statement.setString(count, title);count++;}
            if(type != null) {statement.setString(count, type);count++;}
            if(abs != null) {statement.setString(count, abs);count++;}
            if(projC != null) {statement.setString(count, projC);count++;}
            if(desc != null) {statement.setString(count, desc);count++;}
            statement.setBoolean(count, hasM);count++;
            statement.setString(count, protection);count++;
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
    
    public static boolean updatePR(long mpegfile, int dg_id, String pr){
        try{    
            Connection connection = ConnectionManager.init_connection();
            PreparedStatement statement;
            String query = "UPDATE DatasetGroup set protection = ? where dg_id = ? and mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setString(1,pr);
            statement.setInt(2,dg_id);
            statement.setLong(3, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}       