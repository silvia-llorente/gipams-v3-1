package gcs.Utils;

import java.io.BufferedReader;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class DatasetUtil {

    final static FileUtil f = new FileUtil();
    final static JWTUtil j = new JWTUtil();
    final static MetadataUtil metadataUtil = new MetadataUtil();
    
    private static ByteArrayOutputStream getByteArray(InputStream is){
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
    
    public static int addDT(String jwt, InputStream dt_md_FIS, InputStream dt_pr_FIS, String dgPath, int dg_id, int dt_id, long mpegfile) {
        String owner = JWTUtil.getUID(jwt);
        Boolean hasP = false;
        String[] md;
        try {
            if (dt_md_FIS != null) {
                ByteArrayOutputStream dt_md = getByteArray(dt_md_FIS);
                ByteArrayOutputStream dt_pr = null;
                if (FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(dt_md.toByteArray())),"Dataset")) {
                    md = metadataUtil.parseDT(new String(dt_md.toByteArray()));
                    if(dt_pr_FIS != null) {
                        dt_pr = getByteArray(dt_pr_FIS);
                        if(dt_pr.toByteArray().length != 0 && FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(dt_pr.toByteArray())),"dt_pr")){
                            hasP = true;
                        }
                    }
                    System.out.println("DTUtil: Inserting protection = "+hasP);
                    if(md[0] == null || md[0].equals("-1")){
                        md[0] = String.valueOf(db.consults.dtC.getMaxID(dg_id,mpegfile)+1);
                    }
                    String dtPath = dgPath + File.separator + "dt_"+md[0];
                    System.out.println("DTUtil:Inserting dataset "+md[0]+" to datagroup "+dg_id);
                    String PR = null;
                    if(dt_pr_FIS != null && dt_pr != null) PR = new String(dt_pr.toByteArray());
                    if(db.modifiers.dtM.insertDT(Integer.parseInt(md[0]),md[1],md[2],md[3],md[4],md[5],true,PR,dtPath,owner,dg_id,mpegfile)){
                        FileUtil.createDirectory(dtPath);
                        if(hasP && dt_pr != null){
                            String[] res = ProtectionUtil.encryptMD(mpegfile,dg_id,Integer.parseInt(md[0]),new String(dt_md.toByteArray()),jwt,null);
                            String cipher = res[0];
                            FileUtil.createFile(dtPath + File.separator + "dt_md.xml", cipher);
                            db.modifiers.dtM.updatePR(mpegfile, dg_id, Integer.parseInt(md[0]), res[1]);
                            FileUtil.updateFile(dtPath + File.separator + "dt_pr.xml", res[1]);
                        }
                        else FileUtil.createFile(dtPath + File.separator + "dt_md.xml", new String(dt_md.toByteArray()));
                        System.out.println("DTUtil: Added Dataset to "+dg_id+" and created files.");
                        return Integer.parseInt(md[0]);
                    } else Logger.getLogger(DatasetUtil.class.getName()).log(Level.SEVERE, "Error inserting Dataset to DB");
                } else Logger.getLogger(DatasetUtil.class.getName()).log(Level.SEVERE, "Error in dataset file! Not valid.");
            } else Logger.getLogger(DatasetUtil.class.getName()).log(Level.SEVERE, "Error in dataset file! No file found.");
        } catch (IOException | SAXException | ParserConfigurationException  ex) {
            Logger.getLogger(DatasetUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    public static void deleteDT(int dt_id, int dg_id, long mpegfile, String auth){
        int a = deleteAllKeys(mpegfile,dg_id,dt_id,auth);
        String dtPath = db.consults.dtC.getDTPath(dt_id, dg_id, mpegfile);
        FileUtil.deleteDirectory(dtPath);
        db.modifiers.dtM.deleteDT(dt_id,dg_id,mpegfile); 
    } 

    public static void editDT(String jwt, int dt_id, int dg_id, long mpegfile, InputStream dt_md_FIS, InputStream dt_pr_FIS) {
        String owner = JWTUtil.getUID(jwt);
        boolean hasP = db.consults.dtC.hasProtection(dt_id, dg_id, mpegfile);
        boolean pmod = false;
        String dtPath = db.consults.dtC.getDTPath(dt_id, dg_id, mpegfile);
        if (dt_md_FIS != null) {
            ByteArrayOutputStream dt_md = getByteArray(dt_md_FIS);
            ByteArrayOutputStream dt_pr = null;
            if (FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(dt_md.toByteArray())),"Dataset")) {
                try {
                    String[] md = metadataUtil.parseDT(new String(dt_md.toByteArray()));
                    if(dt_pr_FIS != null){
                        dt_pr = getByteArray(dt_pr_FIS);
                        if(FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(dt_pr.toByteArray())),"dt_pr")) {hasP = true;pmod = true;}
                    }
                    String PR = null;
                    if(dt_pr_FIS != null && dt_pr != null) PR = dt_pr_FIS.toString();
                    if(db.modifiers.dtM.updateDT(Integer.parseInt(md[0]),md[1],md[2],md[3],md[4],md[5],true,PR,dtPath,owner,dg_id,mpegfile)){
                        if(hasP && dt_pr != null && pmod){
                            String[] res = ProtectionUtil.encryptMD(mpegfile,dg_id,Integer.parseInt(md[0]),new String(dt_md.toByteArray()),jwt,null);
                            String cipher = res[0];
                            FileUtil.updateFile(dtPath + File.separator + "dt_md.xml", cipher);
                            db.modifiers.dtM.updatePR(mpegfile, dg_id, Integer.parseInt(md[0]), res[1]);
                            FileUtil.updateFile(dtPath+File.separator+"dt_pr.xml", res[1]);
                        }
                        else FileUtil.updateFile(dtPath+File.separator+"dt_md.xml",new String(dt_md.toByteArray()));
                    }
                } catch (ParserConfigurationException | IOException | SAXException ex) {
                    Logger.getLogger(DatasetUtil.class.getName()).log(Level.SEVERE, "GCS: editDT error!", ex);
                }
            }
        }
    }
    
    public static String getMd(String jwt, int dt_id, int dg_id, long mpegfile) throws IOException {
        String dt_md = FileUtil.getFile(db.consults.dtC.getString(dt_id,"path",dg_id,mpegfile)+File.separator+"dt_md.xml");
        if(db.consults.dtC.hasProtection(dt_id, dg_id, mpegfile)){
            return new String(ProtectionUtil.decryptMD(mpegfile,dg_id,dt_id,dt_md,jwt));
        }
        return FileUtil.getFile(db.consults.dtC.getString(dt_id, "path", dg_id, mpegfile)+File.separator+"dt_md.xml");
    }
    
    public static String getEncMd(String jwt, int dt_id, int dg_id, long mpegfile) throws IOException {
        return FileUtil.getFile(db.consults.dtC.getString(dt_id, "path", dg_id, mpegfile)+File.separator+"dt_md.xml");
    }
    
    public static String getPr(int dt_id, int dg_id, long mpegfile) {
        return FileUtil.getFile(db.consults.dtC.getString(dt_id, "path", dg_id, mpegfile)+File.separator+"dt_pr.xml");
    }
    
    public static int addProtection(long mpegfile, int dg_id, Integer dt_id, InputStream prXML_FIS, String keyType, String algType, String keyName, String policy, String jwt){
        try {
            String dt_mdPath = db.consults.dtC.getDTPath(dt_id, dg_id, mpegfile);
            String dt_md = new String(Files.readAllBytes(Paths.get(dt_mdPath + File.separator + "dt_md.xml")));
            String res[] = null;
            if(prXML_FIS != null) {
                ByteArrayOutputStream dt_pr = getByteArray(prXML_FIS);
                if(FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(dt_pr.toByteArray())), "dt_pr")) {
                    db.modifiers.dtM.updatePR(mpegfile, dg_id, dt_id, new String(dt_pr.toByteArray()));
                    res = ProtectionUtil.encryptMD(mpegfile,dg_id,dt_id,dt_md,jwt,null);
                    FileUtil.updateFile(dt_mdPath + File.separator + "dt_md.xml",res[0]);
                    db.modifiers.dgM.updatePR(mpegfile, dg_id, res[1]);
                    FileUtil.updateFile(dt_mdPath + File.separator + "dt_pr.xml",res[1]);
                    return 0;
                }
            }
            res = ProtectionUtil.encryptMD(mpegfile,dg_id,dt_id,dt_md,jwt,new String[]{algType,keyName,keyType,policy});
            FileUtil.updateFile(dt_mdPath + File.separator + "dt_md.xml",res[0]);
            db.modifiers.dtM.updatePR(mpegfile, dg_id, dt_id, res[1]);
            FileUtil.createFile(dt_mdPath + File.separator + "dt_pr.xml",res[1]);
            return 0;
        } catch (IOException ex) {
            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, "DTUtil: Error addingDTPR", ex);
        }
        return -1;  
    }
    
    private static int deleteAllKeys(long mpegfile, int dg_id, int dt_id, String auth){
        try {
            Properties props = new Properties();
            props.load(ProtectionUtil.class.getClassLoader().getResourceAsStream("app.properties"));
            URL url = new URL(props.getProperty("ps.url") + "/api/v1/deleteAllKeys?dt_id=" + dt_id + "&dg_id=" + dg_id + "&mpegfile=" + mpegfile);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization","Baerer " + auth);
            conn.setRequestMethod("DELETE");
            
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return Integer.parseInt(res);    
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(DatasetUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DatasetUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

}
