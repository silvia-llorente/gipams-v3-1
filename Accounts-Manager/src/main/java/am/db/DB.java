package am.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DB {

    private static String GOOGLE = "GOOGLE";

    public static int GetUserGoogleByGoogleId(String id){
        int googleId = GetAppId(GOOGLE);
        if (googleId != -1) {
            Connection connection = ConnectionManager.init_connection();
            assert connection != null;
            try {
                String query = "SELECT id FROM User where user_id = ? and app = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, id);
                statement.setInt(2, googleId);
                ResultSet result = statement.executeQuery();
                result.next();
                int idUser = result.getInt(1);
                System.out.println("ID " + id + ": " + idUser);

                ConnectionManager.close_connection(connection);

                return idUser;
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                return -1;
            }
        } else {
            return -1;
        }
    }

    public static boolean InsertUserGoogle(String id) {
        int googleId = GetAppId(GOOGLE);
        if (googleId != -1) {
            Connection connection = ConnectionManager.init_connection();
            assert connection != null;
            try {
                String query = "INSERT INTO User(user_id, app) VALUE (?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, id);
                statement.setInt(2, googleId);
                statement.execute();

                ConnectionManager.close_connection(connection);

                return true;
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    private static int GetAppId(String appName) {
        Connection connection = ConnectionManager.init_connection();
        assert connection != null;
        try {
            String query = "SELECT id FROM App where app LIKE ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, appName);
            ResultSet result = statement.executeQuery();
            result.next();
            int id = result.getInt(1);
            System.out.println("ID " + appName + ": " + id);

            ConnectionManager.close_connection(connection);

            return id;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return -1;
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            return -1;
        }
    }

}
