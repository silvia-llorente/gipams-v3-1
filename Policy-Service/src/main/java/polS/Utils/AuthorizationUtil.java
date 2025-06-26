package polS.Utils;

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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorizationUtil {
    private static String authorizationURL = null;
    public static void setProp(String aux){
        AuthorizationUtil.authorizationURL = aux;
    }
    //private final String authorizationURL = "http://authorization-service:8083";
    //private final String authorizationURL = "http://localhost:8083";
    private final String requestSample = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"\n"+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"+
            "xsi:schemaLocation=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17 http://docs.oasis-open.org/xacml/3.0/xacml-core-v3-schema-wd-17.xsd\"\n"+
            "ReturnPolicyIdList=\"false\" CombinedDecision=\"false\">\n"+
            "<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n"+
            "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:role\" IncludeInResult=\"true\">\n"+
            "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">%s</AttributeValue>\n"+
            "</Attribute>\n"+
            "</Attributes>\n"+
            "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n"+
            "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\"\n"+
            "IncludeInResult=\"true\">\n"+

            "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">%s</AttributeValue>\n"+
            "</Attribute>\n"+
            "</Attributes>\n"+
            "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:date\">\n"+
            "<Attribute AttributeId=\"accessDate\" IncludeInResult=\"true\">\n"+
            "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#date\">%s</AttributeValue>\n"+
            "</Attribute>\n"+
            "</Attributes>\n"+
            "</Request>";
    private final JWTUtil jwtUtil = new JWTUtil();

    public boolean authorized2(String baseURL, String resource, String id, String dg_idS, String dt_idS, String auth, String action) {
        if (resource.equals("dt")) {
            int dt_id = Integer.parseInt(dt_idS);
            int dg_id = Integer.parseInt(dg_idS);
            long mpegfile = Long.parseLong(id);
            if(!db.consults.dtC.exists(dt_id,dg_id,mpegfile))return false;
            if (db.consults.dtC.getString(dt_id, "owner", dg_id, mpegfile).equals(jwtUtil.getUID(auth.substring("Bearer".length()).trim()))) return true;
            if (!db.consults.dtC.hasProtection(dt_id, dg_id, mpegfile)) {
                resource = "dg";
                id = String.valueOf(dg_id);
            }
        }
        else if (resource.equals("dg")) {
            int dg_id = Integer.parseInt(dg_idS);
            long mpegID = Long.parseLong(id);
            if(!db.consults.dgC.exists(dg_id, mpegID)) return false;
            if (db.consults.dgC.getString(dg_id, "owner", mpegID).equals(jwtUtil.getUID(auth.substring("Bearer".length()).trim()))) return true;
        }

        else if (resource.equals("file")) {
            long mpegID = Long.parseLong(id);
            if (!db.consults.mpegC.exists(mpegID)) return false;
            return db.consults.mpegC.getString(mpegID, "owner").equals(jwtUtil.getUID(auth.substring("Bearer".length()).trim()));
        }
        else return false;
        try {
            URL url = new URL(baseURL + "/api/v1/files/" + id + "/protection");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , "Bearer "+auth.substring("Bearer".length()).trim());
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String data = br.readLine();
            JsonObject json = JsonParser.parseString(br.readLine()).getAsJsonObject();
            return getAuthorization(action, auth.substring("Bearer".length()).trim(), json.get("data").getAsString());
        } catch (IOException ex) {
            Logger.getLogger(AuthorizationUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private boolean getAuthorization(String action, String jwt, String rules) {
        if(authorizationURL == null) UrlUtil.loadProps();
        Map<String, Object> body = new HashMap();
        body.put("request", getRequest(jwt,action));
        body.put("rule", rules);
        try {
            URL url = new URL(authorizationURL + "/authorize_rule");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , "Bearer "+jwt);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            
            try(OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes());			
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return parseResponse(res);
        } catch (IOException ex) {
            Logger.getLogger(AuthorizationUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization" , "Bearer "+jwt);
//        RestTemplate restTemplate = new RestTemplate();
//        //ResponseEntity<String> response = null;
//        
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(authorizationURL+"/authorize_rule", HttpMethod.POST, requestEntity, String.class);
//            return parseResponse(response.getBody());
//        } catch (IOException | ParserConfigurationException | RestClientException | SAXException e) {
//            System.out.println(e);
//            //e.printStackTrace();
//            return false;
//        }
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization" , "Bearer "+jwt);
//        RestTemplate restTemplate = new RestTemplate();
//        //ResponseEntity<String> response = null;
//        
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(authorizationURL+"/authorize_rule", HttpMethod.POST, requestEntity, String.class);
//            return parseResponse(response.getBody());
//        } catch (IOException | ParserConfigurationException | RestClientException | SAXException e) {
//            System.out.println(e);
//            //e.printStackTrace();
//            return false;
//        }
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization" , "Bearer "+jwt);
//        RestTemplate restTemplate = new RestTemplate();
//        //ResponseEntity<String> response = null;
//        
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(authorizationURL+"/authorize_rule", HttpMethod.POST, requestEntity, String.class);
//            return parseResponse(response.getBody());
//        } catch (IOException | ParserConfigurationException | RestClientException | SAXException e) {
//            System.out.println(e);
//            //e.printStackTrace();
//            return false;
//        }
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization" , "Bearer "+jwt);
//        RestTemplate restTemplate = new RestTemplate();
//        //ResponseEntity<String> response = null;
//        
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(authorizationURL+"/authorize_rule", HttpMethod.POST, requestEntity, String.class);
//            return parseResponse(response.getBody());
//        } catch (IOException | ParserConfigurationException | RestClientException | SAXException e) {
//            System.out.println(e);
//            //e.printStackTrace();
//            return false;
//        }
    }

    private String getRequest(String jwt, String action) {
        JsonObject a = jwtUtil.getRoles(jwt);
        String role = a.get("roles").getAsJsonArray().get(0).getAsString(); //TODO
        String date = "2020-01-01";
        return String.format(requestSample,role,action,date);
    }

    private boolean parseResponse(String response) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(response));
            Document d = builder.parse(is);
            String decision = d.getDocumentElement().getElementsByTagName("Decision").item(0).getTextContent();
            return decision.equals("Permit");
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(AuthorizationUtil.class.getName()).log(Level.SEVERE, "SEARCH: AuthUtil parse response error, response = "+response, ex);
            return false;
        }
    }
}
