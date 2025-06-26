package wm;

import wm.Security.Secured;
import java.io.IOException;
import java.io.InputStream;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import wm.Utils.UrlUtil;


/**
 * JAX-RS resource class that provides operations for authentication.
 *
 * @author Suren Konathala
 */
@Path("/v1")
public class WM_Crypt4GH {

    private static String URLGCSC4GH = null;
    private static String URLHCSC4GH = null;
    public static void setUrlGCSC4GH(String url){
        URLGCSC4GH = url;
    }

    public static void setUrlHCSC4GH(String url) {
        URLHCSC4GH = url; 
    }
	
    @GET
    @Path("/ping3")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }
    
    @POST
    @Secured
    @Path("/crypt4ghEnc")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response Crypt4GHEncrypt(@HeaderParam("Authorization") String auth, 
            @FormDataParam("plain") InputStream plain_FIS) { 
        System.out.println("Estamos entrado a la peticion");
        if(URLGCSC4GH == null) UrlUtil.loadProps();
        
        StreamDataBodyPart plain = null;
        if(plain_FIS != null) plain = new StreamDataBodyPart("plain",plain_FIS);
        else return Response.ok().build();
        String res;
        try (FormDataMultiPart multipart = new FormDataMultiPart()) {
            multipart.bodyPart(plain);            
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            final WebTarget target = client.target(URLGCSC4GH + "/api/v1/encrypt");
            final Response resp = target.request().header("Authorization", auth).post(Entity.entity(multipart, multipart.getMediaType()));
            res = resp.readEntity(String.class);
            multipart.close();
            if(resp.getStatus() == Response.Status.OK.getStatusCode()) return Response.ok(res, MediaType.TEXT_PLAIN).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_Crypt4GH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
    
    @POST
    @Secured
    @Path("/crypt4ghDec")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response Crypt4GHDecrypt(@HeaderParam("Authorization") String auth, 
            @FormDataParam("cipher") InputStream cipher_FIS) { 
        if(URLGCSC4GH == null) UrlUtil.loadProps();
        StreamDataBodyPart cipher = null;
        if(cipher_FIS != null) cipher = new StreamDataBodyPart("cipher",cipher_FIS);
        else return Response.ok().build();
        String res;
        try (FormDataMultiPart multipart = new FormDataMultiPart()) {
            multipart.bodyPart(cipher);            
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            final WebTarget target = client.target(URLGCSC4GH + "/api/v1/decrypt");
            final Response resp = target.request().header("Authorization", auth).post(Entity.entity(multipart, multipart.getMediaType()));
            res = resp.readEntity(String.class);
            multipart.close();
            if(resp.getStatus() == Response.Status.OK.getStatusCode()) return Response.ok(res, MediaType.TEXT_PLAIN).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_Crypt4GH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
}

