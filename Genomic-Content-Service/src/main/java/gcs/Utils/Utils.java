/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gcs.Utils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author alumne
 */
public class Utils {
    public static String encode(byte[] a){
        return Base64.getEncoder().encodeToString(a);
    }
    
    public static byte[] decode(String a){
        return Base64.getDecoder().decode(a.getBytes(StandardCharsets.UTF_8));
    }
    
    public static Element getDocRoot(String xml){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document d = builder.parse(is);
            return d.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, "PS: ProtUtil parser error", ex);
        }
        return null;
    }
    
    public static PrivateKey stringToPK(String key){
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decode(key));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
