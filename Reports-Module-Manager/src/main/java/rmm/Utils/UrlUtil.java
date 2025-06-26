/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmm.Utils;

import rmm.Database.GipamsMongoDB;
import rmm.Security.RestSecurityFilter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
//import wm.WM_SS;

/**
 *
 * @author alumne
 */
public class UrlUtil {
    
//    private static final String PATH = "/opt/GCService/storage";
    
//    public static String getPATH(){
//        return PATH;
//    }
    
    public static void loadProps(){
        try {
            InputStream inputStream = UrlUtil.class.getClassLoader().getResourceAsStream("app.properties");
            Properties props= new Properties();
            props.load(inputStream);
            RestSecurityFilter.setProp(props.getProperty("oauth.url"));
        } catch (IOException ex) {
            Logger.getLogger(UrlUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
