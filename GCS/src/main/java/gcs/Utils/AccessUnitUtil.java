package gcs.Utils;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccessUnitUtil {

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
            Logger.getLogger(AccessUnitUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static int addAU(String jwt, InputStream au_data_FIS, InputStream au_pr_FIS, String dtPath, Integer au_id, int dt_id, int dg_id, long mpegfile) {
        String owner = JWTUtil.getUID(jwt);
        Boolean hasP = false;

        
        try {
            if (au_data_FIS != null) {
                ByteArrayOutputStream au_data = getByteArray(au_data_FIS);
                if(au_data != null && ! au_data.toString().isEmpty()){
                    ByteArrayOutputStream au_pr = null;
                    if(au_pr_FIS != null) {
                        au_pr = getByteArray(au_pr_FIS);
                        if(au_pr.toByteArray().length != 0 && FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(au_pr.toByteArray())),"au_pr")){
                            hasP = true;
                        }
                    }

                    if(au_id == null) au_id = db.consults.auC.getMaxID(dt_id,dg_id,mpegfile)+1;
                    String auPath = dtPath + File.separator + "au_"+au_id;
                    System.out.println("Inserting AU "+au_id+" to dataset "+dt_id);
                    String PR = null;
                    if(au_pr_FIS != null && au_pr != null) PR = new String(au_pr.toByteArray());
                    if(db.modifiers.auM.insertAU(au_id,"typeTest",PR,auPath,owner,dt_id,dg_id,mpegfile)){
                        db.modifiers.auM.insertBlock(1, new String(au_data.toByteArray()).length(), auPath + File.separator + "unaligned", owner, au_id, dt_id, dg_id, mpegfile);
                        FileUtil.createDirectory(auPath);
                        if(hasP && au_pr != null){
                            String pr = new String(au_data.toByteArray());
                            String[] res = ProtectionUtil.encryptAU(mpegfile,dg_id,dt_id,au_id,pr,PR,jwt,null);
                            if (res == null) {
                                Logger.getLogger(AccessUnitUtil.class.getName()).log(Level.SEVERE, "Error afegint protecció al AU.");
                                return -1;
                            }
                            String cipher = res[0];
    //                        FileUtil.createFile(dtPath + File.separator + "au_data.txt", cipher);
    //                        db.modifiers.dtM.updatePR(mpegfile, dg_id, Integer.parseInt(md[0]), res[1]);
                            FileUtil.updateFile(auPath + File.separator + "au_pr.xml", res[1]);
                            db.modifiers.auM.updatePR(mpegfile, dg_id, dt_id, au_id, res[1]);
                            FileUtil.createDirectory(auPath + File.separator + "unaligned");
                            FileUtil.updateFile(auPath + File.separator + "unaligned" + File.separator + String.valueOf(au_id) + ".txt", cipher);
                        } else {
                            FileUtil.createDirectory(auPath + File.separator + "unaligned");
                            FileUtil.updateFile(auPath + File.separator + "unaligned" + File.separator + String.valueOf(au_id) + ".txt", new String(au_data.toByteArray()));
                        }
                        System.out.println("DTUtil: Added AU to "+dt_id+" and created file.");
                        return au_id;
                    } else Logger.getLogger(AccessUnitUtil.class.getName()).log(Level.SEVERE, "Error inserting au to DB");
                }else Logger.getLogger(AccessUnitUtil.class.getName()).log(Level.SEVERE, "Error in au file! Empty file.");
            } else Logger.getLogger(AccessUnitUtil.class.getName()).log(Level.SEVERE, "Error in au file! No file found.");
        } catch (IOException  ex) {
            Logger.getLogger(AccessUnitUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    public static void deleteAU(int au_id, int dt_id, int dg_id, long mpegfile, String auth){
        String auPath = db.consults.auC.getAUPath(au_id, dt_id, dg_id, mpegfile);
        FileUtil.deleteDirectory(auPath);
        db.modifiers.auM.deleteAU(au_id,dt_id,dg_id,mpegfile); 
    } 
    
    public static String getPr(int au_id, int dt_id, int dg_id, long mpegfile) {
        return FileUtil.getFile(db.consults.auC.getAUPath(au_id,dt_id, dg_id, mpegfile)+File.separator+"au_pr.xml");
    }
    
    public static String getData(String jwt, int au_id, int dt_id, int dg_id, long mpegfile) throws IOException {
        String au_data = FileUtil.getFile(db.consults.auC.getAUPath(au_id,dt_id,dg_id,mpegfile)+File.separator+"unaligned/"+au_id+".txt");
        if(db.consults.auC.hasProtection(au_id, dt_id, dg_id, mpegfile)){
            return new String(ProtectionUtil.decryptAU(mpegfile,dg_id,dt_id,au_id,au_data,jwt));
        }
        return FileUtil.getFile(db.consults.auC.getAUPath(au_id, dt_id, dg_id, mpegfile) + File.separator + "unaligned/"+au_id+".txt");
    }
    
    public static String getEncData(String jwt, int au_id, int dt_id, int dg_id, long mpegfile) throws IOException {
        return FileUtil.getFile(db.consults.auC.getAUPath(au_id, dt_id, dg_id, mpegfile) + File.separator + "unaligned/"+au_id+".txt");
    }
    
    public static void deleteBlock(int block_id, int au_id, int dt_id, int dg_id, long mpegfile, String auth){
        String blockPath = db.consults.auC.getBlockPath(block_id, au_id, dt_id, dg_id, mpegfile);
        FileUtil.deleteFile(blockPath + File.separator + block_id + ".txt");
        db.modifiers.auM.deleteBlock(block_id,au_id,dt_id,dg_id,mpegfile); 
    } 
    
//    public static int addProtection(long mpegfile, int dg_id, Integer dt_id, InputStream prXML_FIS, String keyType, String algType, String keyName, String policy, String jwt){
//        try {
//            String dt_mdPath = db.consults.dtC.getDTPath(dt_id, dg_id, mpegfile);
//            String dt_md = new String(Files.readAllBytes(Paths.get(dt_mdPath + File.separator + "dt_md.xml")));
//            String res[] = null;
//            if(prXML_FIS != null) {
//                ByteArrayOutputStream dt_pr = getByteArray(prXML_FIS);
//                if(f.validateXML(new StreamSource(new ByteArrayInputStream(dt_pr.toByteArray())), "dt_pr")) {
//                    db.modifiers.dtM.updatePR(mpegfile, dg_id, dt_id, new String(dt_pr.toByteArray()));
//                    res = ProtectionUtil.encryptMD(mpegfile,dg_id,dt_id,dt_md,jwt,null);
//                    FileUtil.updateFile(dt_mdPath + File.separator + "dt_md.xml",res[0]);
//                    db.modifiers.dgM.updatePR(mpegfile, dg_id, res[1]);
//                    FileUtil.updateFile(dt_mdPath + File.separator + "dt_pr.xml",res[1]);
//                    return 0;
//                }
//            }
//            res = ProtectionUtil.encryptMD(mpegfile,dg_id,dt_id,dt_md,jwt,new String[]{algType,keyName,keyType,policy});
//            FileUtil.updateFile(dt_mdPath + File.separator + "dt_md.xml",res[0]);
//            db.modifiers.dtM.updatePR(mpegfile, dg_id, dt_id, res[1]);
//            FileUtil.createFile(dt_mdPath + File.separator + "dt_pr.xml",res[1]);
//            return 0;
//        } catch (IOException ex) {
//            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, "DTUtil: Error addingDTPR", ex);
//        }
//        return -1;  
//    }
}
