/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gcsc4gh.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import gcsc4gh.Security.RestSecurityFilter;
import java.io.InputStream;

/**
 *
 * @author alumne
 */
public class UrlUtil {
    
    public static void loadProps(){
        try {
            InputStream inputStream = UrlUtil.class.getClassLoader().getResourceAsStream("app.properties");
            Properties props= new Properties();
            props.load(inputStream);
            //PS.setGCSUrl(props.getProperty("GCS.url"));
            RestSecurityFilter.setProp(props.getProperty("oauth.url"));
            AuthorizationUtil.setProp(props.getProperty("authorization.url"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UrlUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UrlUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
