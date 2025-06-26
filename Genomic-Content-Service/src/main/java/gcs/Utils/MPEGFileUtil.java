package gcs.Utils;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MPEGFileUtil {

    final static String PATH = UrlUtil.getPATH();
    
    public static long addMPEGFile(String name, String jwt) {
        long mpegID = db.consults.mpegC.getMaxID()+1;
        String mpegPath = PATH + File.separator + mpegID;
        System.out.println("Creating MPEGFile, ID: "+mpegID+", name: "+name+", owner: "+JWTUtil.getUID(jwt)+" and path: "+mpegPath);
        try {
            if(db.modifiers.mpegM.insertMpegFile(mpegID, name, mpegPath, JWTUtil.getUID(jwt))){
                System.out.println("Inserted metadata to BD...");
                FileUtil.createDirectory(mpegPath);
                return mpegID;
            } else Logger.getLogger(MPEGFileUtil.class.getName()).log(Level.SEVERE, null, "Error inserting MPEGFile to DB");
            
        } catch (FileAlreadyExistsException ex) {
             Logger.getLogger(MPEGFileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public static void deleteMPEGFile(Long id){
        String mpegPath = db.consults.mpegC.getMPEGFilePath(id);
        FileUtil.deleteDirectory(mpegPath);
        db.modifiers.mpegM.deleteMpegFile(id);
    }

}
