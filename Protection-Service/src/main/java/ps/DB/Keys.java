package ps.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

public class Keys {
    public static String DGgetRSAKey(long mpegfile, int dg_id, String name, String type){
        String key = null;
        Connection connection = ConnectionManager.init_connection();
        try{
            String query1 = "select privKey from DGKeys where mpegfile = ? AND dg_id = ? AND name = ?";
            String query2 = "select pubKey from DGKeys where mpegfile = ? AND dg_id = ? AND name = ?";
            PreparedStatement statement = null;
            if(type.equals("priv")) statement = connection.prepareStatement(query1);
            else statement = connection.prepareStatement(query2);
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setString(3, name);
            ResultSet rs = statement.executeQuery();
            
            if(type.equals("priv"))if(rs.next()) key = rs.getString("privKey");
            else if(rs.next()) key = rs.getString("pubKey");
            ConnectionManager.close_connection(connection);
            return key;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: GetDGKey failed", e);
            ConnectionManager.close_connection(connection);
            return key;
        }
    }
    
    public static int DGsetRSAKey(long mpegfile, int dg_id, String name, String privKey, String pubKey){
        Connection connection = ConnectionManager.init_connection();
        int res = 0;
        try{
            String query = "INSERT INTO DGKeys (mpegfile, dg_id, name, privKey, pubKey)"
                    + "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setString(3, name);
            statement.setString(4, privKey);
            statement.setString(5, pubKey);
            res = statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return res;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: Insert DGKey failed", e);
            ConnectionManager.close_connection(connection);
            return res;
        }
    }
    
    public static String DGgetSymKey(long mpegfile, int dg_id, String name){
        String key = null;
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select symKey from DGKeys where mpegfile = ? AND dg_id = ? AND name = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setString(3, name);
            ResultSet rs = statement.executeQuery();
            
            if(rs.next()) key = rs.getString("symKey");
            ConnectionManager.close_connection(connection);
            return key;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: GetDGKey failed", e);
            ConnectionManager.close_connection(connection);
            return key;
        }
    }
    
    public static int DGsetSymKey(long mpegfile, int dg_id, String name, String key) {
        Connection connection = ConnectionManager.init_connection();
        int res = 0;
        try{
            String query = "INSERT INTO DGKeys (mpegfile, dg_id, name, symKey)"
                    + "VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setString(3, name);
            statement.setString(4, key);
            res = statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return res;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: Insert DGSymKey failed", e);
            ConnectionManager.close_connection(connection);
            return res;
        }
    }
    
    
    public static String DTgetRSAKey(long mpegfile, int dg_id, int dt_id, String name, String type){
        String key = null;
        Connection connection = ConnectionManager.init_connection();
        try{
            String query1 = "select privKey from DTKeys where mpegfile = ? AND dg_id = ? AND dt_id = ? AND name = ?";
            String query2 = "select pubKey from DTKeys where mpegfile = ? AND dg_id = ? AND dt_id = ? AND name = ?";
            PreparedStatement statement = null;
            if(type.equals("priv")) statement = connection.prepareStatement(query1);
            else statement = connection.prepareStatement(query2);
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setInt(3, dt_id);
            statement.setString(4, name);
            ResultSet rs = statement.executeQuery();
            
            if(type.equals("priv"))if(rs.next()) key = rs.getString("privKey");
            else if(rs.next()) key = rs.getString("pubKey");
            ConnectionManager.close_connection(connection);
            return key;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: GetDTKey failed", e);
            ConnectionManager.close_connection(connection);
            return key;
        }
    }
    
    public static int DTsetRSAKey(long mpegfile, int dg_id, int dt_id, String name, String privKey, String pubKey){
        Connection connection = ConnectionManager.init_connection();
        int res = 0;
        try{
            String query = "INSERT INTO DTKeys (mpegfile, dg_id, dt_id, name, privKey, pubKey)"
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setInt(3, dt_id);
            statement.setString(4, name);
            statement.setString(5, privKey);
            statement.setString(6, pubKey);
            res = statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return res;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: Insert DTKey failed", e);
            ConnectionManager.close_connection(connection);
            return res;
        }
    }
    
    
    public static String DTgetSymKey(long mpegfile, int dg_id, int dt_id, String name){
        String key = null;
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select symKey from DTKeys where mpegfile = ? AND dg_id = ? AND dt_id = ? AND name = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setInt(3, dt_id);
            statement.setString(4, name);
            ResultSet rs = statement.executeQuery();
            
            if(rs.next()) key = rs.getString("symKey");
            ConnectionManager.close_connection(connection);
            return key;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: GetDTKey failed", e);
            ConnectionManager.close_connection(connection);
            return key;
        }
    }
    
    public static int DTsetSymKey(long mpegfile, int dg_id, int dt_id, String name, String key) {
        Connection connection = ConnectionManager.init_connection();
        int res = 0;
        try{
            String query = "INSERT INTO DTKeys (mpegfile, dg_id, dt_id, name, symKey)"
                    + "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setInt(3, dt_id);
            statement.setString(4, name);
            statement.setString(5, key);
            res = statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return res;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: Insert DTSymKey failed", e);
            ConnectionManager.close_connection(connection);
            return res;
        }
    }
    
    
    public static int DGdeleteAllKeys(long mpegfile, int dg_id){
        Connection connection = ConnectionManager.init_connection();
        int res = 0;
        try{
            String query = "DELETE FROM DGKeys WHERE mpegfile = ? AND dg_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            res = statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return res;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: Delete DGAllKeys failed", e);
            ConnectionManager.close_connection(connection);
            return -1;
        }
    }
    
    public static int DTdeleteAllKeys(long mpegfile, int dg_id, int dt_id){
        Connection connection = ConnectionManager.init_connection();
        int res = 0;
        try{
            String query = "DELETE FROM DTKeys WHERE mpegfile = ? AND dg_id = ? AND dt_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setInt(3, dt_id);
            res = statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return res;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: Delete DTAllKeys failed", e);
            ConnectionManager.close_connection(connection);
            return -1;
        }
    }
    
    
    public static int DGdeleteKey(long mpegfile, int dg_id, String keyName){
        Connection connection = ConnectionManager.init_connection();
        int res = 0;
        try{
            String query = "DELETE FROM DGKeys WHERE mpegfile = ? AND dg_id = ? AND name = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setString(4, keyName);
            res = statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return res;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: Delete DGKey failed", e);
            ConnectionManager.close_connection(connection);
            return -1;
        }
    }
    
    public static int DTdeleteKey(long mpegfile, int dg_id, int dt_id, String keyName){
        Connection connection = ConnectionManager.init_connection();
        int res = 0;
        try{
            String query = "DELETE FROM DTKeys WHERE mpegfile = ? AND dg_id = ? AND dt_id = ? AND name = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setInt(3, dt_id);
            statement.setString(4, keyName);
            res = statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return res;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: Delete DTKey failed", e);
            ConnectionManager.close_connection(connection);
            return -1;
        }
    }
    
    
    public static ResultSet getDTKeys(long mpegfile, int dg_id, int dt_id){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select symKey, privKey, pubKey from DTKeys where mpegfile = ? AND dg_id = ? AND dt_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            statement.setInt(3, dt_id);
            ResultSet rs = statement.executeQuery();
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            ConnectionManager.close_connection(connection);
            return crs;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: GetDTKey failed", e);
            ConnectionManager.close_connection(connection);
            return null;
        }
    }
    
    public static ResultSet getDGKeys(long mpegfile, int dg_id){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select symKey, privKey from DGKeys where mpegfile = ? AND dg_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            
            statement.setLong(1, mpegfile);
            statement.setInt(2, dg_id);
            ResultSet rs = statement.executeQuery();
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            ConnectionManager.close_connection(connection);
            return crs;
        } catch (SQLException e){
            Logger.getLogger(Keys.class.getName()).log(Level.SEVERE, "PS: GetDGKey failed", e);
            ConnectionManager.close_connection(connection);
            return null;
        }
    }
}
