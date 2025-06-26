package gcs.Utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class ProtectionUtil {
    private static String genPSKey(String type, long mpegfile, int dg_id, Integer dt_id, String keyName, int size, String rsaType, String jwt, boolean noStore){
        try {
            Properties props = new Properties();
            props.load(ProtectionUtil.class.getClassLoader().getResourceAsStream("app.properties"));
            URL url = new URL(props.getProperty("ps.url") + "/api/v1/dg/genSymKey?mpegfile="+mpegfile+"&dg_id="+dg_id+"&keyName="+keyName+"&size="+size+"&noStore="+noStore);
            if(dt_id != null){
                url = new URL(props.getProperty("ps.url") + "/api/v1/dt/genSymKey?mpegfile="+mpegfile+"&dg_id="+dg_id+"&dt_id="+dt_id+"&keyName="+keyName+"&size="+size+"&noStore="+noStore);
                if(type.equals("ka")) url = new URL(props.getProperty("ps.url") + "/api/v1/dt/genRSAKey?mpegfile="+mpegfile+"&dg_id="+dg_id+"&dt_id="+dt_id+"&keyName="+keyName+"&type="+rsaType);
            } else if(type.equals("ka")) url = new URL(props.getProperty("ps.url") + "/api/v1/dg/genRSAKey?mpegfile="+mpegfile+"&dg_id="+dg_id+"&keyName="+keyName+"&type="+rsaType);           
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization","Bearer "+jwt);
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
            }   InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            System.out.println(res);
            return res;
        } catch (IOException ex) {
            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, "GCS ProtUtil: Could not load properties", ex);
        }
        return null;
    }
    
    private static String getPSKey(String type, long mpegfile, int dg_id, Integer dt_id, String keyName, String rsaType, String jwt){
        try {
            Properties props = new Properties();
            props.load(ProtectionUtil.class.getClassLoader().getResourceAsStream("app.properties"));
            URL url = new URL(props.getProperty("ps.url") + "/api/v1/dg/getSymKey?mpegfile="+mpegfile+"&dg_id="+dg_id+"&keyName="+keyName);
            if(dt_id != null){
                url = new URL(props.getProperty("ps.url") + "/api/v1/dt/getSymKey?mpegfile="+mpegfile+"&dg_id="+dg_id+"&dt_id="+dt_id+"&keyName="+keyName);
                if(type.equals("ka")) url = new URL(props.getProperty("ps.url") + "/api/v1/dg/getRSAKey?mpegfile="+mpegfile+"&dg_id="+dg_id+"&dt_id="+dt_id+"&keyName="+keyName+"&type="+rsaType);
            } else if(type.equals("ka")) url = new URL(props.getProperty("ps.url") + "/api/v1/dg/getRSAKey?mpegfile="+mpegfile+"&dg_id="+dg_id+"&keyName="+keyName+"&type="+rsaType);           
            System.out.println(url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization","Bearer "+jwt);
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
            }   InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            System.out.println(res);
            return res;
        } catch (IOException ex) {
            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, "GCS ProtUtil: Could not load properties", ex);
        }
        return null;
    }
    
    public static String[] encryptMD(long mpegfile, int dg_id, Integer dt_id, String dg_md, String jwt, String[] formParams){
        String xml = null;
        if(dt_id == null) xml = db.consults.dgC.getPR(mpegfile, dg_id);
        else xml = db.consults.dtC.getPR(mpegfile, dg_id, dt_id);
        if(xml == null)xml = "";
        if(!xml.equals("")){
            String key = null;
            String[][] keys = getEncParams(xml);
            if(keys != null){
                String[] firstParams = null, firstTransport = null;
                for(int i = 0; i < keys.length; ++i){
                    String encLoc = keys[i][4];
                    if(encLoc.equals("metadata")){
                        String keyName = keys[i][1];
                        Map<Integer, String[]> kt = getKeyTransport(xml,keyName);
                        for(String[] line : kt.values()){ //TODO: Falta fer unwrap i esas cosas no?
                            if(!line[0].equals("KeyAsymetricWrap")){
                                if(firstParams == null) {firstParams = keys[i]; firstTransport = line;}
                                key = getPSKey("",mpegfile,dg_id,dt_id,line[2],null,jwt);
                                if(key != null && ! key.isEmpty()) break; 
                            } else if(line[0].equals("KeyAsymetricWrap")){
                                if(firstParams == null) {firstParams = keys[i]; firstTransport = line;}
                                key = getPSKey("ka",mpegfile,dg_id,dt_id,line[3],"pub",jwt);
                                if(key != null && ! key.isEmpty()) break;
                            }
                        }
                        if(key != null && ! key.isEmpty()) break;
                    }
                }
                if(key == null || key.isEmpty()){
                    if(firstParams != null){
                        String algorithm = firstParams[0], keyName = firstParams[1], IV = firstParams[2], TAG = firstParams[3];
                        switch (firstTransport[0]) {
                            case "KeyDerivation":
                                String PRF = firstTransport[1], passwordName = firstTransport[2], salt = firstTransport[3], iterations = firstTransport[4], length = firstTransport[5];
                                String pw = genPSKey("kd",mpegfile,dg_id,dt_id,passwordName,getKeySize(algorithm),null,jwt,false);
                                key = EncryptionUtil.PBKDF2(pw.toCharArray(),PRF,salt,Integer.valueOf(iterations),Integer.valueOf(length));
                                xml = updatePRDer(dt_id,keyName,PRF,passwordName,salt,Integer.valueOf(iterations),Integer.valueOf(length),genEncParams(algorithm,keyName,IV,TAG),getPolicy(xml));
                                break;
                            case "KeySymetricWrap":
                                String kekName = firstTransport[1];
                                String kek = genPSKey("ks",mpegfile,dg_id,dt_id,kekName,getKeySize(algorithm),null,jwt,false);
                                key = genPSKey("ks",mpegfile,dg_id,dt_id,firstParams[1],getKeySize(algorithm),null,jwt,true);
                                String wrKey = EncryptionUtil.wrapAES(kek, key);
                                System.out.println("WRKEY: "+wrKey);
                                System.out.println("KEY: "+key);
                                System.out.println("IV: "+IV);
                                xml = updatePRSym(dt_id,keyName,kekName,wrKey,genEncParams(algorithm,keyName,IV,TAG),getPolicy(xml));
                                break;
                            case "KeyAsymetricWrap":
                                String hFunc = firstTransport[1], MGF = firstTransport[2], pubKName = firstTransport[3];
                                key = genPSKey("ks",mpegfile,dg_id,dt_id,firstParams[1],getKeySize(firstParams[0]),null,jwt,true);
                                String pub = genPSKey("ka",mpegfile,dg_id,dt_id,firstParams[1],getKeySize(firstParams[0]),"pub",jwt,false);
                                wrKey = EncryptionUtil.unwrapRSA(hFunc, MGF, Utils.stringToPK(pub), Utils.decode(key));
                                xml = updatePRAsym(dt_id,keyName,hFunc,MGF,pubKName,wrKey,genEncParams(algorithm,keyName,IV,TAG),getPolicy(xml));
                                break;
                            default:
                                break;
                        }
                    }
                }
                if(key != null && ! key.isEmpty()) {
                    if(firstParams != null) {
                        
                        byte[] result = EncryptionUtil.EncryptAES(Utils.decode(key),Utils.decode(firstParams[2]),firstParams[3],dg_md,firstParams[0]);
                        String decoded = Utils.encode(result);
                        return new String[]{decoded,xml};
                    }
                }
            }
            if (formParams != null){
                String algorithm = formParams[0], keyName = formParams[1], policy = formParams[3].substring(38), IV = EncryptionUtil.genIV(), TAG = EncryptionUtil.genIV();
                switch (formParams[2]) {
                    case "der":
                        String PRF = "urn:mpeg:mpeg-g:protection:hmac-sha512", passwordName = keyName+"Key", salt = EncryptionUtil.genIV(), 
                                iterations = "10000", length = "32";
                        String pw = genPSKey("kd",mpegfile,dg_id,dt_id,passwordName,getKeySize(algorithm),null,jwt,false);
                        key = EncryptionUtil.PBKDF2(pw.toCharArray(),PRF,salt,Integer.valueOf(iterations),Integer.valueOf(length));
                        xml = updatePRDer(dt_id,keyName,PRF,passwordName,salt,Integer.valueOf(iterations),Integer.valueOf(length),genEncParams(algorithm,keyName,IV,TAG),policy);
                        break;
                    case "sym":
                        String kekName = keyName+"Key";
                        String kek = genPSKey("ks",mpegfile,dg_id,dt_id,kekName,getKeySize(algorithm),null,jwt,false);
                        key = genPSKey("ks",mpegfile,dg_id,dt_id,kekName,getKeySize(algorithm),null,jwt,true);
                        String wrKey = EncryptionUtil.wrapAES(kek, key);
                        xml = updatePRSym(dt_id,keyName,kekName,wrKey,genEncParams(algorithm,keyName,IV,TAG),policy);//TODO: Put policy new
                        break;
                    case "asym":
                        String hFunc = "urn:mpeg:mpeg-g:protection:sha512", MGF = "urn:mpeg:mpeg-g:protection:sha256", pubKName = keyName+"Key";
                        key = genPSKey("ks",mpegfile,dg_id,dt_id,pubKName,getKeySize(algorithm),null,jwt,true);
                        String pub = genPSKey("ka",mpegfile,dg_id,dt_id,pubKName,getKeySize(algorithm),"pub",jwt,false);
                        wrKey = EncryptionUtil.unwrapRSA(hFunc, MGF, Utils.stringToPK(pub), Utils.decode(key));
                        xml = updatePRAsym(dt_id,keyName,hFunc,MGF,pubKName,wrKey,genEncParams(algorithm,keyName,IV,TAG),policy);
                        break;
                    default:
                        break;
                }
                return new String[]{Utils.encode(EncryptionUtil.EncryptAES(Utils.decode(key),Utils.decode(IV),TAG,dg_md,algorithm)),xml};
            }
        }
        return new String[]{dg_md,xml};
    }
    
    public static String[] encryptAU(long mpegfile, int dg_id, int dt_id, int au_id, String plain, String pr, String jwt, String[] formParams){
        String xml = db.consults.dtC.getPR(mpegfile, dg_id, dt_id);
        
        String[][] DTEncParams = getEncParams(xml);
        for(int i = 0; i < DTEncParams.length; ++i){
            String confID = DTEncParams[i][5];
            JsonObject jo = getAUEncParams(pr);
            if(jo != null){
                JsonArray ja = jo.getAsJsonArray("keys");
                for(JsonElement e : ja){
                    JsonObject o = e.getAsJsonObject();
                    if(confID.equals(o.get("configurationID").getAsString())){
                        String cipher = jo.get("cipher").getAsString();
                        String aublockIV = jo.get("aublockIV").getAsString();
                        String aublockTAG = null, auinIV = null, auinTAG = null;
                        if(! jo.get("aublockTAG").isJsonNull()) aublockTAG = jo.get("aublockTAG").getAsString();
                        if(! jo.get("auinIV").isJsonNull()) auinIV = jo.get("auinIV").getAsString();
                        if(! jo.get("auinTAG").isJsonNull()) auinTAG = jo.get("auinTAG").getAsString();
                        String kek = getKeyFromKeyTransport(xml,mpegfile,dg_id,dt_id,confID,jwt);
                        String encKey = genPSKey("ks",mpegfile,dg_id,dt_id,"",getKeySize(cipher),null,jwt,true);
                        String wrKey = EncryptionUtil.wrapAES(kek, encKey);
                        //String wrKey = o.get("wrappedKey").getAsString();
                        //String key = getKeyFromKeyTransport(xml,mpegfile,dg_id,dt_id,wrKey,jwt);
                        String newPr = genAUPr(confID,wrKey,cipher,aublockIV,aublockTAG,auinIV,auinTAG);
                        
                        byte[] ciphered = EncryptionUtil.EncryptAES(Utils.decode(encKey), Utils.decode(aublockIV), aublockTAG, plain, cipher);
                        String ciphertext = null;
                        if(ciphered != null) ciphertext = Utils.encode(ciphered);
                        return new String[]{ciphertext,newPr};
                    }
                }
            }
        }
        return null;
    }
    
    private static String updatePRDer(Integer dt_id, String keyName, String PRF, String passwordName, String salt, int iterations, int length, String encP, String policy){
        //TODO
        String head = FileUtil.dgprHead();
        if(dt_id != null) head = FileUtil.dtprHead();
        String foot = FileUtil.dgprFoot();
        if(dt_id != null) foot = FileUtil.dtprFoot();
        return head +
"    <KeyTransportAES>\n" +
"        <keyName>"+keyName+"</keyName>\n" +
"        <KeyDerivation>\n" +
"            <PRF>"+PRF+"</PRF>\n" +
"            <passwordName>"+passwordName+"</passwordName>\n" +
"            <salt>"+salt+"</salt>\n" +
"            <iterations>"+iterations+"</iterations>\n" +
"            <length>"+length+"</length>            \n" +
"        </KeyDerivation>\n" +
"    </KeyTransportAES>\n" +
                encP + "\n" +
                policy + "\n" +
                foot;
    }
    
    private static String updatePRSym(Integer dt_id, String keyName, String kek, String wrappedKey, String encP, String policy){ 
        //TODO: generate xml
        String head = FileUtil.dgprHead();
        if(dt_id != null) head = FileUtil.dtprHead();
        String foot = FileUtil.dgprFoot();
        if(dt_id != null) foot = FileUtil.dtprFoot();
        return head +
"    <KeyTransportAES>\n" +
"        <keyName>"+keyName+"</keyName>\n" +
"        <KeySymmetricWrap>\n" +
"            <kek>"+kek+"</kek>\n" +
"            <wrappedKey>"+wrappedKey+"</wrappedKey>\n" +
"        </KeySymmetricWrap>\n" +
"    </KeyTransportAES>\n" +
                encP + "\n" +
                policy + "\n" +
                foot;

    }
    
    private static String updatePRAsym(Integer dt_id, String keyName, String hashFunction, String MGF, String rsaName, String wrKey, String encP, String policy){
        //TODO: Generar xml
        String head = FileUtil.dgprHead();
        if(dt_id != null) head = FileUtil.dtprHead();
        String foot = FileUtil.dgprFoot();
        if(dt_id != null) foot = FileUtil.dtprFoot();
        return head +
"    <KeyTransportAES>\n" +
"        <keyName>"+keyName+"</keyName>\n" +
"        <KeyAsymmetricWrap>\n" +
"            <hashFunction>"+hashFunction+"</hashFunction>\n" +
"            <maskGenerationHashFunction>"+MGF+"</maskGenerationHashFunction>\n" +
"            <publicKeyName>"+rsaName+"</publicKeyName>\n" +
"            <wrappedKey>"+wrKey+"</wrappedKey>\n" +
"        </KeyAsymmetricWrap>\n" +
"    </KeyTransportAES>\n" +
                encP +
                policy +
                foot;
    }
    
    private static String genEncParams(String cipher, String keyName, String IV, String TAG){
        return "    <EncryptionParameters encryptedLocations=\"metadata\" configurationID=\"19\">\n" +
"        <cipher>"+cipher+"</cipher>\n" +
"        <keyName>"+keyName+"</keyName>\n" +
"        <IV>"+IV+"</IV>\n" +
"        <TAG>"+TAG+"</TAG>\n" +
"    </EncryptionParameters>";
    }
    
    private static String genAUPr(String confID, String wrKey, String cipher, String aublockIV, String aublockTAG, String auinIV, String auinTAG){
        String a = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<AccessUnitProtection xmlns=\"urn:mpeg:mpeg-g:protection:access-unit:2019\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:mpeg:mpeg-g:protection:access-unit:2019 /opt/GCService/schemas/ISOIEC_23092-3_Annex_A5_aupr_schema_SL.xsd\">\n" +
                "    <AccessUnitEncryptionParameters>\n" +
                "	<wrappedKey configurationID=\""+confID+"\">"+wrKey+"</wrappedKey>\n" +
                "        <cipher>"+cipher+"</cipher>\n" +
                "        <aublockIV>"+aublockIV+"</aublockIV>\n" +
                "        <aublockTAG>"+aublockTAG+"</aublockTAG>\n";
        if(auinIV != null) a += "       <auinIV>"+auinIV+"</auinIV>\n";
        if(auinTAG != null) a += "       <auinTAG>"+auinTAG+"</auinTAG>\n";
        return a + "    </AccessUnitEncryptionParameters>\n" +
                "</AccessUnitProtection>";
    }
    
    private static String getPolicy(String xml){
        Element root = Utils.getDocRoot(xml);
        Node p = root.getElementsByTagName("Policy").item(0);
        if (p != null){
            DOMImplementationLS lsImpl = (DOMImplementationLS)p.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
            LSSerializer lsSerializer = lsImpl.createLSSerializer();
            return "    "+lsSerializer.writeToString(p).substring(39);
        }
        return "";
        
    }
    
    public static byte[] decryptMD(long mpegfile, int dg_id, Integer dt_id, String cipherText, String jwt){
        String xml = null;
        if(dt_id == null) xml = db.consults.dgC.getPR(mpegfile, dg_id);
        else xml = db.consults.dtC.getPR(mpegfile, dg_id, dt_id);
        if(xml != null && !xml.equals("")){
            String[][] keys = getEncParams(xml);
            if(keys != null){
                for(int i = 0; i < keys.length; ++i){
                    String encLoc = keys[i][4];
                    if(encLoc.equals("metadata")){
                        String keyName = keys[i][1];
                        Map<Integer, String[]> kt = getKeyTransport(xml,keyName);
                        byte[] plain = null;
                        for(String[] line : kt.values()){
                            switch(line[0]){
                                case "KeyDerivation":
                                    String key = getPSKey("kd",mpegfile,dg_id,dt_id,line[2],null,jwt);
                                    if(key == null) break;
                                    String dkey = EncryptionUtil.PBKDF2(key.toCharArray(),line[1],line[3],Integer.valueOf(line[4]),Integer.valueOf(line[5]));
                                    plain = EncryptionUtil.DecryptAES(Utils.decode(dkey),Utils.decode(keys[i][2]),keys[i][3],Utils.decode(cipherText),keys[i][0]);
                                    return plain;
                                case "KeySymetricWrap":
                                    key = getPSKey("ks",mpegfile,dg_id,dt_id,line[1],null,jwt);
                                    if(key == null) break;
                                    String skey = EncryptionUtil.unwrapAES(key, line[2]);
                                    System.out.println("skey:" + skey);
                                    System.out.println("IV:" + keys[i][2] + ", TAG=" + keys[i][3]);

                                    plain = EncryptionUtil.DecryptAES(Utils.decode(skey),Utils.decode(keys[i][2]),keys[i][3],Utils.decode(cipherText),keys[i][0]);
                                    return plain;
                                case "KeyAsymetricWrap":
                                    key = getPSKey("ka",mpegfile,dg_id,dt_id,line[3],"priv",jwt);
                                    if(key == null) break;
                                    String akey = EncryptionUtil.unwrapRSA(line[1], line[2], Utils.stringToPK(key), Utils.decode(line[4]));
                                    plain = EncryptionUtil.DecryptAES(Utils.decode(akey),Utils.decode(keys[i][2]),keys[i][3],Utils.decode(cipherText),keys[i][0]);
                                    return plain;                                    
                            }
                        }
                    }
                }
            }
            
        }
        return cipherText.getBytes(StandardCharsets.UTF_8);
    }
    
    public static byte[] decryptAU(long mpegfile, int dg_id, int dt_id, Integer au_id, String cipherText, String jwt){
        String xml = db.consults.dtC.getPR(mpegfile, dg_id, dt_id);
        String pr = db.consults.auC.getPR(mpegfile, dg_id, dt_id, au_id);
        
        String[][] DTEncParams = getEncParams(xml);
        for(int i = 0; i < DTEncParams.length; ++i){
            String confID = DTEncParams[i][5];
            JsonObject jo = getAUEncParams(pr);
            if(jo != null){
                JsonArray ja = jo.getAsJsonArray("keys");
                for(JsonElement e : ja){
                    JsonObject o = e.getAsJsonObject();
                    if(confID.equals(o.get("configurationID").getAsString())){ //TODO: GENERATE Encryption Key and regenerate WrappedKey
                        String wrKey = o.get("wrappedKey").getAsString();
                        String key = getKeyFromKeyTransport(xml,mpegfile,dg_id,dt_id,confID,jwt);
                        String encKey = EncryptionUtil.unwrapAES(key, wrKey);
                        String aublockIV = jo.get("aublockIV").getAsString();
                        String aublockTAG = jo.get("aublockTAG").getAsString();
                        String cipher = jo.get("cipher").getAsString();
                        byte[] plain = EncryptionUtil.DecryptAES(Utils.decode(encKey), Utils.decode(aublockIV), aublockTAG, Utils.decode(cipherText), cipher);
                        return plain;
                    }
                }
            }
        }
        return null;
    }
        
    private static String[][] getEncParams(String xml) {
    if (xml == null) return null;
    Element root = Utils.getDocRoot(xml);
    if (root == null) return null;

    NodeList nList = root.getElementsByTagName("EncryptionParameters");
    int n = nList.getLength();
    if (n == 0) return null;

    String[][] ret = new String[n][6];

    for (int i = 0; i < n; ++i) {
        Node encParam = nList.item(i);
        if (encParam.getNodeType() == Node.ELEMENT_NODE) {
            Element encP = (Element) encParam;

            // Llegim atributs
            String encLoc = encP.getAttribute("encryptedLocations");
            String confID = encP.getAttribute("configurationID");

            // Llegim elements fills
            String cipher = null, keyName = null, IV = null, TAG = null;

            Node node = encP.getElementsByTagName("cipher").item(0);
            if (node != null) cipher = node.getTextContent();

            node = encP.getElementsByTagName("keyName").item(0);
            if (node != null) keyName = node.getTextContent();

            node = encP.getElementsByTagName("IV").item(0);
            if (node != null) IV = node.getTextContent();

            node = encP.getElementsByTagName("TAG").item(0);
            if (node != null) TAG = node.getTextContent();

            ret[i] = new String[] { cipher, keyName, IV, TAG, encLoc, confID };
        }
    }

    return ret;
}
    
    /*private static JsonObject getAUEncParams(String xml){
        if(xml == null) return null;
        Element root = Utils.getDocRoot(xml);
        if(root == null) return null;
        int n = root.getElementsByTagName("AccessUnitEncryptionParameters").getLength();
        if (n > 0) {
            String[] ret = new String[6];
            Node encParam = root.getElementsByTagName("AccessUnitEncryptionParameters").item(0);
            JsonObject jo = new JsonObject();
            if(encParam.getNodeType() == Node.ELEMENT_NODE){
                Element encP = (Element) encParam;
                String wrappedKey = null, cipher = null, aublockIV = null, auinIV = null, aublockTAG = null, auinTAG = null, confID = null;
                Node node;
                
                NodeList nl = encP.getElementsByTagName("wrappedKey");
                JsonArray ja = new JsonArray();
                for (int i = 0; i < nl.getLength(); ++i){
                    JsonObject joAux = new JsonObject();
                    node = encP.getElementsByTagName("wrappedKey").item(i);
                    if(node != null) wrappedKey = node.getTextContent();
                    Element wrK = (Element) node;
                    if(wrK != null) confID = wrK.getAttribute("configurationID");
                    joAux.addProperty("wrappedKey", wrappedKey);
                    joAux.addProperty("configurationID", confID);
                    ja.add(joAux);
                }
                cipher = encP.getElementsByTagName("cipher").item(0).getTextContent();
                aublockIV = encP.getElementsByTagName("aublockIV").item(0).getTextContent();
                node = encP.getElementsByTagName("auinIV").item(0);
                if(node != null) auinIV = node.getTextContent();
                node = encP.getElementsByTagName("aublockTAG").item(0);
                if(node != null) aublockTAG = node.getTextContent();
                node = encP.getElementsByTagName("auinTAG").item(0);
                if(node != null) auinTAG = node.getTextContent();
                jo.add("keys", ja);
                jo.addProperty("cipher", cipher);
                jo.addProperty("aublockIV", aublockIV);
                jo.addProperty("auinIV", auinIV);
                jo.addProperty("aublockTAG", aublockTAG);
                jo.addProperty("auinTAG", auinTAG);
            }  
            return jo;
        }
        return null;
    }*/
    private static JsonObject getAUEncParams(String xml) {
        if (xml == null || xml.isBlank()) return null;

        try {
            // 1) Parsear en modo namespace-aware
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder()
                              .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element root = doc.getDocumentElement();
            String NS = root.getNamespaceURI(); // "urn:mpeg:mpeg-g:protection:access-unit:2019"

            // 2) AccessUnitEncryptionParameters (con namespace)
            NodeList paramsList = root.getElementsByTagNameNS(NS, "AccessUnitEncryptionParameters");
            if (paramsList.getLength() == 0) return null;
            Element encP = (Element) paramsList.item(0);

            JsonObject jo = new JsonObject();

            // 3) wrappedKey[]
            NodeList wkList = encP.getElementsByTagNameNS(NS, "wrappedKey");
            JsonArray keysArr = new JsonArray();
            for (int i = 0; i < wkList.getLength(); i++) {
                Element wk = (Element) wkList.item(i);
                JsonObject k = new JsonObject();
                k.addProperty("wrappedKey", wk.getTextContent().trim());
                k.addProperty("configurationID", wk.getAttribute("configurationID"));
                keysArr.add(k);
            }
            if (keysArr.size() > 0) jo.add("keys", keysArr);

            // 4) cipher
            NodeList cList = encP.getElementsByTagNameNS(NS, "cipher");
            if (cList.getLength() > 0) {
                jo.addProperty("cipher", cList.item(0).getTextContent().trim());
            }

            // 5) aublockIV
            NodeList ivList = encP.getElementsByTagNameNS(NS, "aublockIV");
            if (ivList.getLength() > 0) {
                jo.addProperty("aublockIV", ivList.item(0).getTextContent().trim());
            }

            // 6) auinIV, aublockTAG, auinTAG
            for (String tag : new String[]{"auinIV", "aublockTAG", "auinTAG"}) {
                NodeList list = encP.getElementsByTagNameNS(NS, tag);
                if (list.getLength() > 0) {
                    jo.addProperty(tag, list.item(0).getTextContent().trim());
                }
            }

            return jo;

        } catch (Exception e) {
            throw new RuntimeException("Error al parsear XML", e);
        }
    }





        
    private static Map<Integer,String[]> getKeyTransport(String xml, String keyName){
        Element root = Utils.getDocRoot(xml);
        if(root == null) return null;
        int n = root.getElementsByTagName("KeyTransportAES").getLength();
        if (n > 0) {
            Map<Integer,String[]> ret = new HashMap<>();
            for(int i = 0; i < n; ++i){
                Node ktn = root.getElementsByTagName("KeyTransportAES").item(i), node = null;
                if (ktn.getNodeType() == Node.ELEMENT_NODE) {
                    Element kt = (Element) ktn;
                    String ktKeyName = kt.getElementsByTagName("keyName").item(0).getTextContent();
                    if(ktKeyName.equals(keyName)){
                        Node kd = kt.getElementsByTagName("KeyDerivation").item(0);
                        Node ks = kt.getElementsByTagName("KeySymmetricWrap").item(0);
                        Node ka = kt.getElementsByTagName("KeyAsymmetricWrap").item(0);
                        if(kd != null){
                            if(kd.getNodeType() == Node.ELEMENT_NODE){
                                Element kdE = (Element) kd;
                                String PRF = kdE.getElementsByTagName("PRF").item(0).getTextContent();
                                String passwordName = kdE.getElementsByTagName("passwordName").item(0).getTextContent();
                                String salt = kdE.getElementsByTagName("salt").item(0).getTextContent();
                                String iterations = kdE.getElementsByTagName("iterations").item(0).getTextContent();
                                String length = kdE.getElementsByTagName("length").item(0).getTextContent();
                                ret.put(i,new String[]{"KeyDerivation",PRF,passwordName,salt,iterations,length});
                            }
                        }
                        else if(ks != null) {
                            if(ks.getNodeType() == Node.ELEMENT_NODE){
                                Element ksE = (Element) ks;
                                String kek = ksE.getElementsByTagName("kek").item(0).getTextContent();
                                String wrappedKey = ksE.getElementsByTagName("wrappedKey").item(0).getTextContent();
                                ret.put(i,new String[]{"KeySymetricWrap",kek,wrappedKey});
                            }
                        }
                        else if(ka != null) {
                            if(ka.getNodeType() == Node.ELEMENT_NODE){
                                Element kaE = (Element) ka;
                                String hashFunction = kaE.getElementsByTagName("hashFunction").item(0).getTextContent();
                                String maskGenerationHashFunction = kaE.getElementsByTagName("maskGenerationHashFunction").item(0).getTextContent();
                                String publicKeyName = kaE.getElementsByTagName("publicKeyName").item(0).getTextContent();
                                String wrappdKey = kaE.getElementsByTagName("wrappedKey").item(0).getTextContent();
                                ret.put(i,new String[]{"KeyAsymetricWrap",hashFunction,maskGenerationHashFunction,publicKeyName,wrappdKey});
                            }
                        }
                        else return null;
                    }
                }
            }
            return ret;
        }
        return null;
    }
    
    private static String[][] getSignatures(long mpegfile, int dg_id, int dt_id){//TODO
        String xml = null;
        if(dt_id > 0) xml = db.consults.dtC.getPR(mpegfile, dg_id, dt_id);
        else xml = db.consults.dgC.getPR(mpegfile, dg_id);
        Element root = Utils.getDocRoot(xml);
        if(root == null) return null;
        int n = root.getElementsByTagName("SignatureParameters").getLength();
        if (n > 0) {
            String[][] ret = new String[][]{{}};
            for(int i = 0; i < n; ++i){
                Node sig = root.getElementsByTagName("SignatureParameters").item(i);
                Node sigInfo, node;
                String ID = null, canonAlg = null;
//                sigInfo = sig.getAttributes().getNamedItem("SignedInfo").item(i);
//                if(node != null) canonAlg = sigInfo.getNodeValue();
            }
            return ret;
        }
        return null;
    }
    
    private static int getKeySize(String cipher){
        return Integer.valueOf(cipher.substring(cipher.length()-7, cipher.length()-4));
    }
    
    private static String getKeyFromKeyTransport(String xml, long mpegfile, int dg_id, int dt_id, String confID, String jwt){
        String[][] keys = getEncParams(xml);
        if(keys != null){
            for(int i = 0; i < keys.length; ++i){
                String encLoc = keys[i][4];
                if(encLoc.equals("metadata")){
                    String keyName = keys[i][1];
                    if(keys[i][5].equals(confID)){
                        Map<Integer, String[]> kt = getKeyTransport(xml,keyName);
                        for(String[] line : kt.values()){
                            switch(line[0]){
                                case "KeyDerivation":
                                    String key = getPSKey("kd",mpegfile,dg_id,dt_id,line[2],null,jwt);
                                    return key;
//                                    if(key == null) break;
//                                    return EncryptionUtil.PBKDF2(key.toCharArray(),line[1],line[3],Integer.valueOf(line[4]),Integer.valueOf(line[5]));
                                case "KeySymetricWrap":
//                                    if(wrappedKey == null) wrappedKey = line[2];
                                    key = getPSKey("ks",mpegfile,dg_id,dt_id,line[1],null,jwt);
                                    return key;
//                                    if(key == null) break;
//                                    return EncryptionUtil.unwrapAES(key, wrappedKey);
                                case "KeyAsymetricWrap":
//                                    if(wrappedKey == null) wrappedKey = line[4];
                                    key = getPSKey("ka",mpegfile,dg_id,dt_id,line[3],"priv",jwt);
                                    return key;
//                                    if(key == null) break;
//                                    return EncryptionUtil.unwrapRSA(line[1], line[2], Utils.stringToPK(key), Utils.decode(wrappedKey));
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    //DEMO
    public static byte[] decrypt(InputStream cipher_FIS, InputStream pr_FIS, InputStream key_FIS, String auth) {
        String xml = new String(getByteArray(pr_FIS).toByteArray());
        String key = new String(getByteArray(key_FIS).toByteArray());
        String cipherText = new String(getByteArray(cipher_FIS).toByteArray());
        String[][] keys = getEncParams(xml);
        if(keys != null){
            for(int i = 0; i < keys.length; ++i){
                String encLoc = keys[i][4];
                if(encLoc.equals("metadata")){
                    String keyName = keys[i][1];
                    Map<Integer, String[]> kt = getKeyTransport(xml,keyName);
                    byte[] plain = null;
                    for(String[] line : kt.values()){
                        switch(line[0]){
                            case "KeyDerivation":
                                String dkey = EncryptionUtil.PBKDF2(key.toCharArray(),line[1],line[3],Integer.valueOf(line[4]),Integer.valueOf(line[5]));
                                plain = EncryptionUtil.DecryptAES(Utils.decode(dkey),Utils.decode(keys[i][2]),keys[i][3],Utils.decode(cipherText),keys[i][0]);
                                return plain;
                            case "KeySymetricWrap":
                                String skey = EncryptionUtil.unwrapAES(key, line[2]);
                                plain = EncryptionUtil.DecryptAES(Utils.decode(skey),Utils.decode(keys[i][2]),keys[i][3],Utils.decode(cipherText),keys[i][0]);
                                return plain;
                            case "KeyAsymetricWrap":
                                String akey = EncryptionUtil.unwrapRSA(line[1], line[2], Utils.stringToPK(key), Utils.decode(line[4]));
                                plain = EncryptionUtil.DecryptAES(Utils.decode(akey),Utils.decode(keys[i][2]),keys[i][3],Utils.decode(cipherText),keys[i][0]);
                                return plain;                                    
                        }
                    }
                }
            }
        }
        return cipherText.getBytes(StandardCharsets.UTF_8);
    }
    private static ByteArrayOutputStream getByteArray(InputStream is){//DEMO
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            
            return buffer;
        } catch (IOException ex) {
            Logger.getLogger(DatasetUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
