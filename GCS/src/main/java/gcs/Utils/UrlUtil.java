/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gcs.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import gcs.Security.RestSecurityFilter;
import gcs.GCS;
import java.io.InputStream;

/**
 *
 * @author alumne
 */
public class UrlUtil {
    
    private static String PATH = "C:\\test\\opt\\GCService\\storage";
    
    public static String getPATH(){
        return PATH;
    }
    
    public static void loadProps(){
        try {
            InputStream inputStream = UrlUtil.class.getClassLoader().getResourceAsStream("app.properties");
            Properties props= new Properties();
            props.load(inputStream);
            GCS.setGCSUrl(props.getProperty("gcs.url"));
            RestSecurityFilter.setProp(props.getProperty("oauth.url"));
            PATH = props.getProperty("storagePath");
//            AuthorizationUtil2.setProp(props.getProperty("authorization.url"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UrlUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UrlUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
