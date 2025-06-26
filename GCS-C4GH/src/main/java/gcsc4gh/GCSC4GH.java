package gcsc4gh;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import gcsc4gh.Security.Secured;
import gcsc4gh.Utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/v1")
public class GCSC4GH {
    
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }
    
    @POST
    @Secured
    @Path("/encrypt")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response encrypt(@HeaderParam("Authorization") String auth,
            @FormDataParam("plain") InputStream plain_FIS){
        System.out.println("GCSC4GH.java: Hemos llegado");
        if(plain_FIS == null) return Response.status(Response.Status.BAD_REQUEST).build();
        String plain = new String(Utils.getByteArray(plain_FIS).toByteArray());
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("/bin/bash", "/opt/C4GH/util.sh", "encrypt", plain);
        StringBuilder output = new StringBuilder();
        try {
            Process process = processBuilder.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            System.out.println(output.toString());
        } catch (IOException ex) {
            Logger.getLogger(GCSC4GH.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: "+ex);
        }
        
        return Response.ok(output.toString(), MediaType.TEXT_PLAIN).build();
    }
    
    @POST
    @Secured
    @Path("/decrypt")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response decrypt(@HeaderParam("Authorization") String auth,
            @FormDataParam("cipher") InputStream cipher_FIS){
        
        if(cipher_FIS == null) return Response.status(Response.Status.BAD_REQUEST).build();
        byte[] b64 = Utils.getByteArray(cipher_FIS).toByteArray();
        String b64str = new String(b64);
        byte[] cipher = Base64.getDecoder().decode(b64str.getBytes(StandardCharsets.UTF_8));
        File f = new File("/opt/C4GH/M.c4gh");
        try (FileOutputStream outputStream = new FileOutputStream(f)) {
            outputStream.write(cipher);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GCSC4GH.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GCSC4GH.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("/bin/bash", "/opt/C4GH/util.sh", "decrypt");
        StringBuilder output = new StringBuilder();
        try {
            Process process = processBuilder.start();
            String line = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(GCSC4GH.class.getName()).log(Level.SEVERE, null, ex);
        }
        f.delete();
        
        return Response.ok(output.toString(), MediaType.TEXT_PLAIN).build();
    }
    
}