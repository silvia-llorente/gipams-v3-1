package gcs.Utils;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;

import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.validation.Schema;


public class FileUtil {
    private static final String DG_MD_XSD = UrlUtil.getPATH() + "/../schemas/ISOIEC_23092-3_Annex_A1_dgmd_schema.xsd";
    private static final String DT_MD_XSD = UrlUtil.getPATH() + "/../schemas/ISOIEC_23092-3_Annex_A2_dtmd_schema.xsd";
    private static final String DG_PR_XSD = UrlUtil.getPATH() + "/../schemas/ISOIEC_23092-3_Annex_A3_dgpr_schema_SL.xsd";
    private static final String DT_PR_XSD = UrlUtil.getPATH() + "/../schemas/ISOIEC_23092-3_Annex_A4_dtpr_schema_SL.xsd";
    private static final String AU_PR_XSD = UrlUtil.getPATH() + "/../schemas/ISOIEC_23092-3_Annex_A5_aupr_schema_SL.xsd";
    private static final String XENC = UrlUtil.getPATH() + "/../schemas/xenc-schema.xsd";
    private static final String XMLDSIG_CORE = UrlUtil.getPATH() + "/../schemas/xmldsig-core-schema.xsd";

    public FileUtil() {}

    public static String randomFileName() {
        String uuid = UUID.randomUUID().toString();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        return uuid + sdf.format(cal.getTime());
    }

    public static void createDirectory(String path) throws FileAlreadyExistsException {
        boolean created = new File(path).mkdirs();
        if (!created) {
            throw new FileAlreadyExistsException("Directory already exists");
        }
    }

    public static void deleteDirectory(String path){
        if (path == null || path.isEmpty()) {
            System.err.println("Error: El path es nulo o vacío.");
            return;
        }
        File index = new File(path);
        if(index.exists()){
            String[]entries = index.list();
            for(String s: entries){
                File currentFile = new File(index.getPath(),s);
                currentFile.delete();
            }
            index.delete();
        }
    }

    public static void createFile(String path, String content) {
        File f = new File(path);
        try {
            if (f.createNewFile()) {
                try (FileWriter writer = new FileWriter(path)) {
                    writer.write(content);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void updateFile(String path, String content) {
        try {
            File f = new File(path);
            f.createNewFile();
            try (FileWriter writer = new FileWriter(path)) {
                //System.out.println("Updating file "+path);
                writer.write(content);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, "GCS: FileUtil could not update file", ex);
        }
    }
    
    public static void deleteFile(String path){
        File f = new File(path);
        f.delete();
    }

    public static String getFile(String path) {
        try {
            if (Files.exists(Paths.get(path))) {
                return new String(Files.readAllBytes(Paths.get(path)));
            } else {
                Logger.getLogger(FileUtil.class.getName()).log(Level.WARNING, "File does not exist: " + path);
                return "";
            }
        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, "Error reading file: " + path, ex);
            return null;
        }
    }


    public static boolean validateXML(StreamSource xml, String type) {
        switch (type) {
            case "DatasetGroup":
                return validateXMLSchema(DG_MD_XSD,xml);
            case "Dataset": 
                return validateXMLSchema(DT_MD_XSD,xml);
            case "dg_pr":
                return validateXMLSchema(DG_PR_XSD,xml);
            case "dt_pr":
                return validateXMLSchema(DT_PR_XSD,xml);
            case "au_pr":
                return validateXMLSchema(AU_PR_XSD,xml);
            default:
                return false;
        }
    }
    
    private static boolean validateXMLSchema(String xsdPath, StreamSource xmlSource) {
        try {
            System.out.println("Validando:" + xsdPath);
            // Forzar implementación del JDK (no la del servidor)
            SchemaFactory factory = SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI,
                "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory",
                null
            );

            // Cargar el esquema
            Schema schema = factory.newSchema(new File(xsdPath));

            // Crear el validador
            Validator validator = schema.newValidator();

            validator.validate(xmlSource);

        } catch (IOException | SAXException e) {
            System.out.println("Exception: " + e.getMessage());
            return false;
        }
        return true;
    }
    
    
    public static String dgprHead(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<DatasetGroupProtection xmlns=\"urn:mpeg:mpeg-g:protection:dataset-group:2019\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"       \n" +
"    xsi:schemaLocation=\"urn:mpeg:mpeg-g:protection:dataset-group:2019 /opt/GCService/schemas/ISOIEC_23092-3_Annex_A3_dgpr_schema_SL.xsd\">";
    }
    
    public static String dgprFoot(){
        return "</DatasetGroupProtection>";  
    }
    
    public static String dtprHead(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<DatasetProtection xmlns=\"urn:mpeg:mpeg-g:protection:dataset:2019\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"       \n" +
"    xsi:schemaLocation=\"urn:mpeg:mpeg-g:protection:dataset:2019 /opt/GCService/schemas/ISOIEC_23092-3_Annex_A4_dtpr_schema_SL.xsd\">";
    }
    
    public static String dtprFoot(){
        return "</DatasetProtection>";  
    }
    private static boolean checkInternetByPing() {
        try {
            InetAddress address = InetAddress.getByName("8.8.8.8"); // Google DNS
            return address.isReachable(3000); // Timeout 3s
        } catch (IOException e) {
            return false;
        }
    }
    private static boolean checkInternetByHttp(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000); // Timeout 3s
            connection.connect();
            int code = connection.getResponseCode();
            return (code == 200 || code == 301 || code == 302); // HTTP OK o Redirect
        } catch (Exception e) {
            return false;
        }
    }
}
