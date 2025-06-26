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

public class DatasetGroupUtil {

    final static FileUtil f = new FileUtil();
    final static ProtectionUtil p = new ProtectionUtil();
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
    
    public static int addDG(String jwt, InputStream dg_md_FIS, InputStream dg_pr_FIS, Long mpegID, String mpegPath) {
        String owner = j.getUID(jwt);
        Boolean hasP = false;
        String[] md;
        try {
            if (dg_md_FIS != null){
                ByteArrayOutputStream dg_md = getByteArray(dg_md_FIS);
                ByteArrayOutputStream dg_pr = null;
                if(f.validateXML(new StreamSource(new ByteArrayInputStream(dg_md.toByteArray())),"DatasetGroup")){
                    
                    md = metadataUtil.parseDG(new String(dg_md.toByteArray()));
                    if(dg_pr_FIS != null) {
                        dg_pr = getByteArray(dg_pr_FIS);
                        System.out.println("hola");
                        if(f.validateXML(new StreamSource(new ByteArrayInputStream(dg_pr.toByteArray())),"dg_pr")){
                            hasP = true;
                        }
                    }
                    System.out.println("DGUtil: Inserting protection = "+hasP);
                    if(md[0] == null || md[0].equals("-1")){
                        md[0] = String.valueOf(db.consults.dgC.getMaxID(mpegID)+1);
                    }
                    String dgPath = mpegPath + File.separator + "dg_"+md[0];
                    String PR = null;
                    if(dg_pr_FIS != null && dg_pr != null) PR = new String(dg_pr.toByteArray());
                    if(db.modifiers.dgM.insertDG(Integer.parseInt(md[0]),md[1],md[2],md[3],md[4],md[5],true,PR,dgPath,owner,mpegID)){
                        f.createDirectory(dgPath);
                        if(hasP && dg_pr != null){
                            //String[]{algType,keyName,keyType,policy}
                            String res[] = ProtectionUtil.encryptMD(mpegID,Integer.parseInt(md[0]),null,new String(dg_md.toByteArray()),jwt,null);
                           

                            String cipher = res[0];
                            FileUtil.updateFile(dgPath + File.separator + "dg_pr.xml", res[1]);
                            db.modifiers.dgM.updatePR(mpegID, Integer.parseInt(md[0]), res[1]);
                            FileUtil.createFile(dgPath + File.separator + "dg_md.xml", cipher);
                        }
                        else 
                        FileUtil.createFile(dgPath + File.separator + "dg_md.xml", new String(dg_md.toByteArray()));
                                
                        System.out.println("DGUtil: DatasetGroup added to BD and files created");
                        return Integer.parseInt(md[0]);
                    } else Logger.getLogger(DatasetGroupUtil.class.getName()).log(Level.SEVERE, "Error inserting DatasetGroup to DB");
                } else Logger.getLogger(DatasetGroupUtil.class.getName()).log(Level.SEVERE, "Error in datasetGroup file! Not valid.");
            } else Logger.getLogger(DatasetGroupUtil.class.getName()).log(Level.SEVERE, "Error in datasetGroup file! Null file.");
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(DatasetGroupUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public static void deleteDG(int dg_id, long mpegfile, String auth){
        int res = deleteAllKeys(mpegfile, dg_id, auth);
        String dgPath = db.consults.dgC.getDGPath(dg_id,mpegfile);
        System.out.println("DatasetGroupUtil.java (GCS) -> dgPath: " + dgPath);
        
        FileUtil.deleteDirectory(dgPath);
        
        db.modifiers.dgM.deleteDG(dg_id,mpegfile);     
    }

    public static void editDG(String jwt, int dg_id, long mpegfile, InputStream dg_md_FIS, InputStream dg_pr_FIS) throws Exception {
        boolean hasP = db.consults.dgC.hasProtection(dg_id,mpegfile);
        boolean pmod = false;
        String owner = JWTUtil.getUID(jwt);
        String dgPath = db.consults.dgC.getDGPath(dg_id,mpegfile);
        if (dg_md_FIS != null) {
            ByteArrayOutputStream dg_md = getByteArray(dg_md_FIS);
            ByteArrayOutputStream dg_pr = null;
            if (FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(dg_md.toByteArray())),"DatasetGroup")){
                String[] md = metadataUtil.parseDG(new String(dg_md.toByteArray()));
                if (dg_pr_FIS != null) {
                    dg_pr = getByteArray(dg_pr_FIS);
                    if(FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(dg_pr.toByteArray())),"dg_pr")) {hasP = true; pmod = true;}
                }
                String PR = null;
                if(dg_pr_FIS != null && dg_pr != null) PR = dg_pr_FIS.toString();
                if(db.modifiers.dgM.updateDG(Integer.parseInt(md[0]),md[1],md[2],md[3],md[4],md[5],true,PR,dgPath,owner,mpegfile)){
                    if(hasP && dg_pr != null && pmod){
                        //new String[]{algType,keyName,keyType,policy}
                        String[] res = ProtectionUtil.encryptMD(mpegfile,Integer.parseInt(md[0]),null,new String(dg_md.toByteArray()),jwt,null);
                        String cipher = res[0];
                        FileUtil.updateFile(dgPath+File.separator+"dg_md.xml",cipher);
                        db.modifiers.dgM.updatePR(mpegfile, dg_id, res[1]);
                        FileUtil.updateFile(dgPath+File.separator+"dg_pr.xml",res[1]);
                    }
                    else FileUtil.updateFile(dgPath+File.separator+"dg_md.xml",new String(dg_md.toByteArray()));
                }
            }
        }
        
    }
    
    public static String getMd(String jwt, int dg_id, long mpegfile) throws IOException {
        String dg_md = FileUtil.getFile(db.consults.dgC.getString(dg_id,"path",mpegfile)+File.separator+"dg_md.xml");
        if(db.consults.dgC.hasProtection(dg_id, mpegfile)){
            return new String(ProtectionUtil.decryptMD(mpegfile,dg_id,null,dg_md,jwt));
        }
        return FileUtil.getFile(db.consults.dgC.getString(dg_id,"path",mpegfile)+File.separator+"dg_md.xml");
    }
    
    public static String getEncMd(String jwt, int dg_id, long mpegfile) throws IOException {
        return FileUtil.getFile(db.consults.dgC.getString(dg_id,"path",mpegfile)+File.separator+"dg_md.xml");
    }
    
    public static String getPr(int dg_id, long mpegfile) {
        return FileUtil.getFile(db.consults.dgC.getString(dg_id,"path",mpegfile)+File.separator+"dg_pr.xml");
    }
    
    public static int addProtection(long mpegfile, int dg_id, InputStream prXML_FIS, String keyType, String algType, String keyName, String policy, String jwt){
        try {
            String dg_mdPath = db.consults.dgC.getDGPath(dg_id, mpegfile);
            String dg_md = new String(Files.readAllBytes(Paths.get(dg_mdPath + File.separator + "dg_md.xml")));
            String res[] = null;
            if(prXML_FIS != null) {
                ByteArrayOutputStream dg_pr = getByteArray(prXML_FIS);
                if(FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(dg_pr.toByteArray())), "dg_pr")) {
                    db.modifiers.dgM.updatePR(mpegfile, dg_id, new String(dg_pr.toByteArray()));
                    res = ProtectionUtil.encryptMD(mpegfile,dg_id,null,dg_md,jwt,null);
                    FileUtil.updateFile(dg_mdPath + File.separator + "dg_md.xml",res[0]);
                    db.modifiers.dgM.updatePR(mpegfile, dg_id, res[1]);
                    FileUtil.updateFile(dg_mdPath + File.separator + "dg_pr.xml",res[1]);
                    return 0;
                }
            }
            res = ProtectionUtil.encryptMD(mpegfile,dg_id,null,null,jwt,new String[]{algType,keyName,keyType,policy});
            FileUtil.updateFile(dg_mdPath + File.separator + "dg_md.xml",res[0]);
            db.modifiers.dgM.updatePR(mpegfile, dg_id, res[1]);
            FileUtil.updateFile(dg_mdPath + File.separator + "dg_pr.xml",res[1]);
            return 0;
        } catch (IOException ex) {
            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, "DGUtil: Error addingDGPR", ex);
        }
        return -1;  
    }
    
    private static int deleteAllKeys(long mpegfile, int dg_id, String auth){
        try {
            Properties props = new Properties();
            props.load(ProtectionUtil.class.getClassLoader().getResourceAsStream("app.properties"));
            URL url = new URL(props.getProperty("ps.url") + "/api/v1/deleteAllKeys?dg_id=" + dg_id + "&mpegfile=" + mpegfile);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("DELETE");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return Integer.parseInt(res);    
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(DatasetGroupUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DatasetGroupUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
}
