package db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author alumne
 */
public class Main {
    
    public static void main(){
        System.out.println("Has dt: "+db.consults.dgC.hasDT(1,9));
//        try {
//            Connection conn = db.connManager.ConnectionManager.init_connection();
//            String query = "select * from MPEGFile";
//            PreparedStatement st = conn.prepareStatement(query);
//            ResultSet rs = st.executeQuery();
//            while(rs.next()){
//                System.out.println(rs.toString());
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
