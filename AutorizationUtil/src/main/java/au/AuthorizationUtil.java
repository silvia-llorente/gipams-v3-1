package au;

import com.google.gson.*;
import java.io.BufferedReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class AuthorizationUtil {
    private static final String REQ_SAMPLE = new BufferedReader(
            new InputStreamReader(AuthorizationUtil.class.getClassLoader().getResourceAsStream("xacml-req-template.xml"), StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining("\n"));
    
    private static String getOwner(String token){
        String[] chunks = token.split("\\.");
        String dec = new String(Base64.getDecoder().decode(chunks[1]));
        JsonObject json =  JsonParser.parseString(dec).getAsJsonObject();
        return json.get("sub").getAsString();
    }
    
    private static JsonObject getRoles(String token) {
        String[] chunks = token.split("\\.");
        String dec = new String(Base64.getDecoder().decode(chunks[1]));
        JsonObject json = JsonParser.parseString(dec).getAsJsonObject();
        return json.get("realm_access").getAsJsonObject();
    }
    
    private static long[] getDTAuths(int dg_id, long mpegfile, String token){
        ResultSet rs = db.consults.dgC.getDT(dg_id, mpegfile);
        Set<Long> dts = new HashSet<>();
        try {
            while(rs.next()){
                int dt_id = rs.getInt("dt_id");
                if(getAuthorization("GetHierarchy", token, db.consults.dtC.getPR(mpegfile, dg_id, dt_id))){//check every dataset
                    dts.add(Long.valueOf(dt_id));
                }
            }
            if(dts.isEmpty()) return null;
            long ret[] = new long[dts.size()];
            Iterator<Long> itr = dts.iterator();
            int i = 0;
            while(itr.hasNext()){
                ret[i] = itr.next();
                ++i;
            }
            return ret;
        } catch (SQLException ex) {
            Logger.getLogger(AuthorizationUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;       
    }
    
    public static boolean authorizeModification(String resource, String mpegfileS, String dg_idS, String dt_idS, String jwt){ //TODO: Explicar a la memoria
        String owner = getOwner(jwt);
        long mpegfile = Long.parseLong(mpegfileS);
        if(resource.equals("file")) return db.consults.mpegC.exists(mpegfile) && db.consults.mpegC.getString(mpegfile, "owner").equals(owner);
        int dg_id = Integer.parseInt(dg_idS);
        if(resource.equals("dg")) return db.consults.dgC.exists(dg_id, mpegfile) && db.consults.dgC.getString(dg_id, "owner", mpegfile).equals(owner);
        int dt_id = Integer.parseInt(dt_idS);
        if(resource.equals("dt")) return db.consults.dtC.exists(dt_id, dg_id, mpegfile) && db.consults.dtC.getString(dt_id, "owner", dg_id, mpegfile).equals(owner);
        return false;
    }
    
    public static long[] authorized(String baseURL, String resource, String mpegfileS, String dg_idS, String dt_idS, String auth, String action) {
        String token = auth.substring("Bearer".length()).trim();
        long mpegfile = Long.parseLong(mpegfileS);
        Integer dg_id = null, dt_id = null;
        switch (resource) {
            case "file":
                if (db.consults.mpegC.exists(mpegfile) && db.consults.mpegC.getString(mpegfile, "owner").equals(getOwner(token))) return new long[]{mpegfile};
                else return null;
            case "dg":
                if(dg_idS == null) return null;
                dg_id = Integer.parseInt(dg_idS);
                if(!db.consults.dgC.exists(dg_id, mpegfile)) return null;
                if (db.consults.dgC.getString(dg_id, "owner", mpegfile).equals(getOwner(token))) return new long[]{dg_id};
                if (!db.consults.dgC.hasProtection(dg_id, mpegfile) || ! hasPolicy(db.consults.dgC.getPR(mpegfile, dg_id))){
                        return new long[]{dg_id};
                } else if(getAuthorization(action, token, getPolicy(db.consults.dgC.getPR(mpegfile, dg_id)))){
                        return new long[]{dg_id};
                } 
                return null;
            case "dt":
                if(dt_idS == null || dg_idS == null) return null;
                dt_id = Integer.parseInt(dt_idS);
                dg_id = Integer.parseInt(dg_idS);
                
                if(!db.consults.dtC.exists(dt_id,dg_id,mpegfile)) return null;
                if (db.consults.dtC.getString(dt_id, "owner", dg_id, mpegfile).equals(getOwner(token))) return new long[]{dt_id};
                if (!db.consults.dtC.hasProtection(dt_id, dg_id, mpegfile) || ! hasPolicy(db.consults.dtC.getPR(mpegfile, dg_id, dt_id))){
                        return new long[]{dt_id};
                } else if(getAuthorization(action, token, getPolicy(db.consults.dtC.getPR(mpegfile, dg_id, dt_id))))
                    return new long[]{dt_id};
                return null;
            default:
                return null;
        }
    }
    
    private static boolean getAuthorization(String action, String token, String policy) {
        try {
            Properties props = new Properties();
            props.load(AuthorizationUtil.class.getClassLoader().getResourceAsStream("app.properties"));
            String authorizationURL = props.getProperty("authorization.url");
            boolean authorized = false;
            JsonArray roles = getRoles(token).getAsJsonArray("roles");
            for(int i = 0; i < roles.size() && !authorized; ++i){
                String date = java.time.LocalDate.now().toString();
                String body = "request=" + String.format(REQ_SAMPLE,roles.get(i).getAsString(),action,date) + "&policy=" + policy;
                try {
                    URL url = new URL(authorizationURL + "/api/v1/authorize_rule");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Authorization" , "Bearer " + token);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    
                    try(OutputStream os = conn.getOutputStream()) {			
                        os.write(body.getBytes("utf-8"));
                    }
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());
                    BufferedReader br = new BufferedReader(in);
                    String res = "",aux;
                    while((aux = br.readLine()) != null)
                        res += aux;
                    authorized = parseResponse(res);
                } catch (IOException ex) {
                    Logger.getLogger(AuthorizationUtil.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
            return authorized;
        } catch (IOException ex) {
            Logger.getLogger(AuthorizationUtil.class.getName()).log(Level.SEVERE, "No properties file", ex);
        }
        return false;
    }

    private static boolean parseResponse(String response) { //TODO: Això s'hauria de fer a l'authorization maybe
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(response));
            Document d = builder.parse(is);
            String decision = d.getDocumentElement().getElementsByTagName("Decision").item(0).getTextContent();
            return decision.equals("Permit");
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(AuthorizationUtil.class.getName()).log(Level.SEVERE, "GCS: AuthUtil parse response error, response = "+response, ex);
            return false;
        }
    }
    
    private static boolean hasPolicy(String xml){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document d = builder.parse(is);
            Element root = d.getDocumentElement();
            NodeList p = root.getElementsByTagName("Policy");
            if(p.getLength() > 0) return true;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(AuthorizationUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private static String getPolicy(String xml){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document d = builder.parse(is);
            Element root = d.getDocumentElement();
            Node p = root.getElementsByTagName("Policy").item(0);
            DOMImplementationLS lsImpl = (DOMImplementationLS)p.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
            LSSerializer lsSerializer = lsImpl.createLSSerializer();
            return lsSerializer.writeToString(p).substring(39);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(AuthorizationUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
