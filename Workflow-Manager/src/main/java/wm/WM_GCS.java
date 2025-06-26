package wm;

import java.io.BufferedReader;
import wm.Security.Secured;
import wm.Utils.JWTUtil;
import java.io.IOException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import wm.Utils.UrlUtil;
import wm.Utils.JWTUtil;

/**
 * JAX-RS resource class that provides operations for authentication.
 *
 * @author Suren Konathala
 */
@Path("/v1")
public class WM_GCS {
    private final JWTUtil jwtUtil = new JWTUtil();

    private static String URLGCS = null;
    public static void setGCSUrl(String url){
        URLGCS = url;
    }
	
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }
    
    @POST
    @Secured
    @Path("/addFile")
    public Response addMPEGFile(@HeaderParam("Authorization") String auth, 
            @FormParam("file_name") String file_name) {
        
        
        if (!file_name.matches("(?:[?\\dA-Za-zÀ-ÿ0-9])+$")) return Response.status(Response.Status.BAD_REQUEST).build();
        if (file_name.length() > 15) return Response.status(Response.Status.BAD_REQUEST).build();
        
        String body = "file_name=" + file_name;
        
        try {
            if(URLGCS == null) UrlUtil.loadProps();
            URL url = new URL(URLGCS + "/api/v1/addFile");
            System.out.println(url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            try(OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());			
            } catch (IOException ex) {
                Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, "WM: Writting socket", ex);
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            System.out.println(res);
            return Response.ok(res,MediaType.APPLICATION_JSON).build();
        } catch (ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, "WM: Protocol exception", ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, "WM: malformed url", ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, "WM: IO exception", ex);
        }
        return Response.serverError().build();
    }

    @POST
    @Secured
    @Path("/addDatasetGroup")
    public Response addDatasetGroup(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dg_md") InputStream dg_md_FIS,
            @FormDataParam("dg_pr") InputStream dg_pr_FIS, 
            @FormDataParam("dt_md") InputStream dt_md_FIS,
            @FormDataParam("dt_pr") InputStream dt_pr_FIS,
            @FormDataParam("au_data") InputStream au_data_FIS,
            @FormDataParam("au_pr") InputStream au_pr_FIS,
            @FormDataParam("file_id") String file_id) {
        if(URLGCS == null) UrlUtil.loadProps();
        if (!au.AuthorizationUtil.authorizeModification("file", file_id, null, null, JWTUtil.getToken(auth))) return Response.status(Response.Status.FORBIDDEN).build();
                
        if (dg_md_FIS == null || dg_pr_FIS == null) return Response.status(Response.Status.BAD_REQUEST).build();
        StreamDataBodyPart dg_md = new StreamDataBodyPart("dg_md",dg_md_FIS);
        StreamDataBodyPart dg_pr = new StreamDataBodyPart("dg_pr",dg_pr_FIS);
        StreamDataBodyPart dt_md = null, dt_pr = null, au_data = null, au_pr = null;
        if(dt_md_FIS != null) dt_md = new StreamDataBodyPart("dt_md",dt_md_FIS);
        if(dt_pr_FIS != null) dt_pr = new StreamDataBodyPart("dt_pr",dt_pr_FIS);
        if(au_data_FIS != null) au_data = new StreamDataBodyPart("au_data",au_data_FIS);
        if(au_pr_FIS != null) au_pr = new StreamDataBodyPart("au_pr",au_pr_FIS);
        
        final FormDataMultiPart multipart;
        String res;
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            
            multipart = (FormDataMultiPart) formDataMultiPart
                    .field("file_id", file_id)
                    .bodyPart(dg_md)
                    .bodyPart(dg_pr);
            if(dt_md_FIS != null && dt_md != null) multipart.bodyPart(dt_md);
            if(dt_pr_FIS != null && dt_pr != null) multipart.bodyPart(dt_pr);
            if(au_data_FIS != null && au_data != null) multipart.bodyPart(au_data);
            if(au_pr_FIS != null && au_pr != null) multipart.bodyPart(au_pr);
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            if(URLGCS == null) UrlUtil.loadProps();
            final WebTarget target = client.target(URLGCS + "/api/v1/addDatasetGroup");
            MediaType mediaType = new MediaType(
                multipart.getMediaType().getType(),
                multipart.getMediaType().getSubtype());
            final Response resp = target.request().header("Authorization", auth).post(Entity.entity(multipart,mediaType));
            res = resp.readEntity(String.class);
            multipart.close();
//            System.out.println(res);
            if(resp.getStatus() == Response.Status.OK.getStatusCode()) return Response.ok(res, MediaType.TEXT_PLAIN).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }

    @POST
    @Secured
    @Path("/addDataset")
    public Response addDataset(@HeaderParam("Authorization") String auth, 
            @FormDataParam("au_data") InputStream au_data_FIS, 
            @FormDataParam("au_pr") InputStream au_pr_FIS,
            @FormDataParam("dt_md") InputStream dt_md_FIS, 
            @FormDataParam("dt_pr") InputStream dt_pr_FIS, 
            @FormDataParam("mpegfile") String mpegfile,
            @FormDataParam("dg_id") String dg_id) {
        if(URLGCS == null) UrlUtil.loadProps();
        if(!au.AuthorizationUtil.authorizeModification("dg", mpegfile, dg_id, null, JWTUtil.getToken(auth))) return Response.status(Response.Status.FORBIDDEN).build();

        StreamDataBodyPart dt_md = null, dt_pr = null, au_data = null, au_pr = null;
        if(dt_md_FIS != null) dt_md = new StreamDataBodyPart("dt_md",dt_md_FIS);
        if(dt_pr_FIS != null) dt_pr = new StreamDataBodyPart("dt_pr",dt_pr_FIS);
        if(au_data_FIS != null) au_data = new StreamDataBodyPart("au_data",au_data_FIS);
        if(au_pr_FIS != null) au_pr = new StreamDataBodyPart("au_pr",au_pr_FIS);
        
        final FormDataMultiPart multipart;
        String res;
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            
            multipart = (FormDataMultiPart) formDataMultiPart.field("dg_id", dg_id).field("mpegfile", mpegfile);
            if (dt_md_FIS == null || dt_pr_FIS == null) return Response.status(Response.Status.BAD_REQUEST).build();
            multipart.bodyPart(dt_md).bodyPart(dt_pr);
            if(au_data_FIS != null && au_data != null) multipart.bodyPart(au_data);
            if(au_pr_FIS != null && au_pr != null) multipart.bodyPart(au_pr);
            
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            if(URLGCS == null) UrlUtil.loadProps();
            final WebTarget target = client.target(URLGCS + "/api/v1/addDataset");
            MediaType mediaType = new MediaType(
                multipart.getMediaType().getType(),
                multipart.getMediaType().getSubtype());
            final Response resp = target.request().header("Authorization", auth).post(Entity.entity(multipart,mediaType));
            res = resp.readEntity(String.class);
            multipart.close();
//            System.out.println(res);
            if(resp.getStatus() == Response.Status.OK.getStatusCode()) return Response.ok(res, MediaType.TEXT_PLAIN).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
    
    @POST
    @Secured
    @Path("/addAccessUnit")
    public Response addAccessUnit(@HeaderParam("Authorization") String auth, 
            @FormDataParam("au_data") InputStream au_data_FIS, 
            @FormDataParam("au_pr") InputStream au_pr_FIS, 
            @FormDataParam("mpegfile") String mpegfile,
            @FormDataParam("dg_id") String dg_id,
            @FormDataParam("dt_id") String dt_id) {
        if(URLGCS == null) UrlUtil.loadProps();
        if(!au.AuthorizationUtil.authorizeModification("dt", mpegfile, dg_id, dt_id, JWTUtil.getToken(auth))) return Response.status(Response.Status.FORBIDDEN).build();

        StreamDataBodyPart au_data = null, au_pr = null;
        if(au_data_FIS != null) au_data = new StreamDataBodyPart("au_data",au_data_FIS);
        if(au_pr_FIS != null) au_pr = new StreamDataBodyPart("au_pr",au_pr_FIS);
        
        final FormDataMultiPart multipart;
        String res;
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            
            multipart = (FormDataMultiPart) formDataMultiPart.field("dt_id", dt_id).field("dg_id", dg_id).field("mpegfile", mpegfile);
            if (au_data_FIS == null || au_pr_FIS == null) return Response.status(Response.Status.BAD_REQUEST).build();
            multipart.bodyPart(au_data).bodyPart(au_pr);
            
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            if(URLGCS == null) UrlUtil.loadProps();
            final WebTarget target = client.target(URLGCS + "/api/v1/addAccessUnit");
            MediaType mediaType = new MediaType(
                multipart.getMediaType().getType(),
                multipart.getMediaType().getSubtype());
            final Response resp = target.request().header("Authorization", auth).post(Entity.entity(multipart,mediaType));
            res = resp.readEntity(String.class);
            multipart.close();
//            System.out.println(res);
            if(resp.getStatus() == Response.Status.OK.getStatusCode()) return Response.ok(res, MediaType.TEXT_PLAIN).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
    
    @POST
    @Secured
    @Path("/addProtection")
    public Response addProtection(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dt_pr") InputStream dt_pr_FIS, 
            @FormDataParam("dg_pr") InputStream dg_pr_FIS, 
            @FormDataParam("keyType") String keyType, 
            @FormDataParam("algType") String algType,
            @FormDataParam("keyName") String keyName,
            @FormDataParam("role") String role,
            @FormDataParam("action") String action,
            @FormDataParam("date") String date,
            @FormDataParam("mpegfile") String mpegfile,
            @FormDataParam("dg_id") String dg_id,
            @FormDataParam("dt_id") String dt_id) {
        if(URLGCS == null) UrlUtil.loadProps();
        boolean authorized = false;
        if(dt_id == null || !dt_id.isEmpty()) authorized = au.AuthorizationUtil.authorizeModification("dg", mpegfile, dg_id, null, JWTUtil.getToken(auth));
        else authorized = au.AuthorizationUtil.authorizeModification("dt", mpegfile, dg_id, dt_id, JWTUtil.getToken(auth));
        if (!authorized) return Response.status(Response.Status.FORBIDDEN).build();
        
        boolean hasP = false;
        if(dt_id != null && ! dt_id.isEmpty()) hasP = db.consults.dtC.hasProtection(Integer.parseInt(dt_id), Integer.parseInt(dg_id), Long.parseLong(mpegfile));
        else hasP = db.consults.dgC.hasProtection(Integer.parseInt(dg_id), Long.parseLong(mpegfile));
        if(hasP) return Response.status(Response.Status.NOT_MODIFIED).build();
        
        String policy = "";
        try {
            Properties props = new Properties();
            props.load(WM_GCS.class.getClassLoader().getResourceAsStream("app.properties"));
            URL url = new URL(props.getProperty("polS.url") + "/api/v1/forgePolicy?role=" + role + "&action=" + action + "&date=" + date);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String aux = null;
            while((aux = br.readLine()) != null)
                policy += aux;
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        StreamDataBodyPart dg_pr = null;
        if(dg_pr_FIS != null) dg_pr = new StreamDataBodyPart("dg_pr",dg_pr_FIS);
        StreamDataBodyPart dt_pr = null;
        if(dt_pr_FIS != null) dt_pr = new StreamDataBodyPart("dt_pr",dt_pr_FIS);
        
        final FormDataMultiPart multipart;
        String res;
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            
            multipart = (FormDataMultiPart) formDataMultiPart.field("dg_id", dg_id).field("mpegfile", mpegfile).field("dt_id", dt_id)
                    .field("keyType", keyType).field("algType", algType).field("keyName", keyName).field("policy", policy);
            if (dt_pr_FIS == null && dg_pr_FIS == null && algType == null && keyType == null) return Response.status(Response.Status.BAD_REQUEST).build();
            if(dt_pr_FIS != null) multipart.bodyPart(dt_pr).bodyPart(dt_pr);
            else if(dg_pr_FIS != null) multipart.bodyPart(dg_pr).bodyPart(dg_pr);
            
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            if(URLGCS == null) UrlUtil.loadProps();
            final WebTarget target = client.target(URLGCS + "/api/v1/addProtection");
            MediaType mediaType = new MediaType(
                multipart.getMediaType().getType(),
                multipart.getMediaType().getSubtype());
            final Response resp = target.request().header("Authorization", auth).post(Entity.entity(multipart,mediaType));
            res = resp.readEntity(String.class);
            multipart.close();
            System.out.println(res);
            if(resp.getStatus() == Response.Status.OK.getStatusCode()) return Response.ok(res, MediaType.TEXT_PLAIN).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, "WMGCS: AddProtection ERROR", ex);
        }
        return Response.serverError().build();
    }

    @POST
    @Secured
    @Path("/editDatasetGroup")
    public Response editDatasetGroup(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dg_md") InputStream dg_md_FIS, 
            @FormDataParam("dg_pr") InputStream dg_pr_FIS, 
            @FormDataParam("mpegfile") String mpegfile,
            @FormDataParam("dg_id") String dg_id) {
        if (dg_md_FIS == null && dg_pr_FIS == null) return Response.status(Response.Status.BAD_REQUEST).build();
        
        if(!au.AuthorizationUtil.authorizeModification("dg", mpegfile, dg_id, null, JWTUtil.getToken(auth))) return Response.status(Response.Status.FORBIDDEN).build();
        
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("dg_id", dg_id).field("mpegfile", mpegfile);
            
            if(URLGCS == null) UrlUtil.loadProps();
            if (dg_md_FIS != null) {
                multipart.bodyPart(new StreamDataBodyPart("dg_md",dg_md_FIS));
            }
            if (dg_pr_FIS != null) {
                multipart.bodyPart(new StreamDataBodyPart("dg_pr",dg_pr_FIS));
            }
            
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            final WebTarget target = client.target(URLGCS + "/api/v1/editDatasetGroup");
            MediaType mediaType = new MediaType(
                multipart.getMediaType().getType(),
                multipart.getMediaType().getSubtype());
            final Response resp = target.request().header("Authorization", auth).post(Entity.entity(multipart,mediaType));
            String res = resp.readEntity(String.class);
            multipart.close();
            if(resp.getStatus() == Response.Status.OK.getStatusCode()) return Response.ok(res, MediaType.TEXT_PLAIN).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, "EditDG IOExcp", ex);
        }
        return Response.serverError().build(); 
    }

    @POST
    @Secured
    @Path("/editDataset")
    public Response editDataset(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dt_md") InputStream dt_md_FIS, 
            @FormDataParam("dt_pr") InputStream dt_pr_FIS, 
            @FormDataParam("mpegfile") String mpegfile,
            @FormDataParam("dg_id") String dg_id,
            @FormDataParam("dt_id") String dt_id) {
        if (dt_md_FIS == null && dt_pr_FIS == null) return Response.status(Response.Status.BAD_REQUEST).build();
        
        if(!au.AuthorizationUtil.authorizeModification("dt", mpegfile, dg_id, dt_id, JWTUtil.getToken(auth))) return Response.status(Response.Status.FORBIDDEN).build();
        
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("dt_id", dt_id).field("dg_id", dg_id).field("mpegfile", mpegfile);
            if(URLGCS == null) UrlUtil.loadProps();
            if (dt_md_FIS != null) {
                multipart.bodyPart(new StreamDataBodyPart("dt_md",dt_md_FIS));
            }
            if (dt_pr_FIS != null) {
                multipart.bodyPart(new StreamDataBodyPart("dt_pr",dt_pr_FIS));
            }
            
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            final WebTarget target = client.target(URLGCS + "/api/v1/editDataset");
            MediaType mediaType = new MediaType(
                multipart.getMediaType().getType(),
                multipart.getMediaType().getSubtype());
            final Response resp = target.request().header("Authorization", auth).post(Entity.entity(multipart,mediaType));
            String res = resp.readEntity(String.class);
            multipart.close();
            if(resp.getStatus() == Response.Status.OK.getStatusCode()) return Response.ok(res, MediaType.TEXT_PLAIN).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }

    @DELETE
    @Secured
    @Path("/deleteFile")
    public Response deleteFile(@HeaderParam("Authorization") String auth, 
            @QueryParam("file_id") String file_id) {
        //System.out.println(db.consults.mpegC.exists(Long.parseLong(file_id)));
        if(URLGCS == null) UrlUtil.loadProps();
        
        if(!au.AuthorizationUtil.authorizeModification("file", file_id, null, null, JWTUtil.getToken(auth))) return Response.status(Response.Status.FORBIDDEN).build();
        
        try {
            URL url = new URL(URLGCS + "/api/v1/deleteFile?file_id=" + file_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("DELETE");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            System.out.println(res);
            return Response.ok(res,MediaType.APPLICATION_JSON).build();    
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }

    @DELETE
    @Secured
    @Path("/deleteDatasetGroup")
    public Response deleteDatasetGroup(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfile,
            @QueryParam("dg_id") String dg_id) {
        if(URLGCS == null) UrlUtil.loadProps();
        if(!au.AuthorizationUtil.authorizeModification("dg", mpegfile, dg_id, null, JWTUtil.getToken(auth))) return Response.status(Response.Status.FORBIDDEN).build();
        
        try {
            URL url = new URL(URLGCS + "/api/v1/deleteDatasetGroup?dg_id=" + dg_id + "&mpegfile=" + mpegfile);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("DELETE");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            System.out.println(res);
            return Response.ok(res,MediaType.APPLICATION_JSON).build();    
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }

    @DELETE
    @Secured
    @Path("/deleteDataset")
    public Response deleteDataset(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfile,
            @QueryParam("dg_id") String dg_id,
            @QueryParam("dt_id") String dt_id) {
        if(URLGCS == null) UrlUtil.loadProps();
        if(!au.AuthorizationUtil.authorizeModification("dt", mpegfile, dg_id, dt_id, JWTUtil.getToken(auth))) return Response.status(Response.Status.FORBIDDEN).build();
        
        try {
            URL url = new URL(URLGCS + "/api/v1/deleteDataset?dt_id=" + dt_id + "&dg_id=" + dg_id + "&mpegfile=" + mpegfile);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("DELETE");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            System.out.println(res);
            return Response.ok(res,MediaType.APPLICATION_JSON).build();    
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }

    @GET
    @Secured
    @Path("/mpegfile")
    public Response getFile(@HeaderParam("Authorization") String auth, 
            @QueryParam("file_id") String file_id) {
        if(URLGCS == null) UrlUtil.loadProps();
        if(!au.AuthorizationUtil.authorizeModification("file", file_id, null, null, JWTUtil.getToken(auth))) return Response.status(Response.Status.FORBIDDEN).build();
        
        try {
            URL url = new URL(URLGCS + "/api/v1/mpegfile?file_id=" + file_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            System.out.println("url testing: " + url);
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            System.out.println(res);
            return Response.ok(res,MediaType.APPLICATION_JSON).build();    
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }

    @GET
    @Secured
    @Path("/dg")
    public Response getDatasetGroup(@HeaderParam("Authorization") String auth,
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS, 
            @QueryParam("resource") String resource) {
        int dg_id = Integer.parseInt(dg_idS);
        long mpegfile = Long.parseLong(mpegfileS);
        if (!db.consults.dgC.exists(dg_id,mpegfile)) return Response.status(Response.Status.BAD_REQUEST).build();
        if(URLGCS == null) UrlUtil.loadProps();
        String action = null;
        switch (resource) {
            case "GetMetadataContentDG":
                action = "GetMetadataContentDG";
                break;
            case "GetProtectionDG":
                action = "GetProtectionDG";
                break;
            case "GetHierarchy":
                action = "GetHierarchy";
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (action != null && ! action.equals("GetProtectionDG")) {
            long[] authRes = au.AuthorizationUtil.authorized(URLGCS, "dg", mpegfileS, dg_idS, null, auth, action);
            if (authRes == null || authRes[0] < 0 || (int) authRes[0] != Integer.parseInt(dg_idS)) return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        try {
            URL url = new URL(URLGCS + "/api/v1/dg?dg_id=" + dg_idS + "&mpegfile=" + mpegfileS + "&resource=" + action);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return Response.ok(res,MediaType.APPLICATION_JSON).build();    
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
    
    @GET
    @Secured
    @Path("/dgEnc")
    public Response getEncDatasetGroup(@HeaderParam("Authorization") String auth,
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS) {
        if(URLGCS == null) UrlUtil.loadProps();
//        String action = "GetMetadataContentDG";
//        long[] authRes = au.AuthorizationUtil.authorized(URLGCS, "dg", mpegfileS, dg_idS, null, auth, action);
//        if (authRes == null || authRes[0] < 0 || (int) authRes[0] != Integer.parseInt(dg_idS)) return Response.status(Response.Status.FORBIDDEN).build();
        try {
            URL url = new URL(URLGCS + "/api/v1/dgEnc?dg_id=" + dg_idS + "&mpegfile=" + mpegfileS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return Response.ok(res,MediaType.APPLICATION_JSON).build();    
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }

    @Secured
    @GET
    @Path("/dt")
    public Response getDataset(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("dt_id") String dt_id, 
            @QueryParam("resource") String resource) {
        if(URLGCS == null) UrlUtil.loadProps();
        String action;
        switch (resource) {
            case "GetMetadataContent":
                action = "GetMetadataContent";
                break;
            case "GetProtection":
                action = "GetProtection";
                break;
            case "GetHierarchy":
                action = "GetHierarchy";
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if(!action.equals("GetProtection")){
            long[] authRes = au.AuthorizationUtil.authorized(URLGCS, "dt", mpegfileS, dg_idS, dt_id, auth, action);
            if (authRes == null || authRes[0] < 0 || (int) authRes[0] != Integer.parseInt(dt_id)) return Response.status(Response.Status.FORBIDDEN).build();
        }
        try {
            URL url = new URL(URLGCS + "/api/v1/dt?dt_id=" + dt_id + "&dg_id=" + dg_idS + "&mpegfile=" + mpegfileS + "&resource=" + action);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return Response.ok(res,MediaType.APPLICATION_JSON).build();
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
    
    @Secured
    @GET
    @Path("/dtEnc")
    public Response getEncDataset(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("dt_id") String dt_id) {
        if(URLGCS == null) UrlUtil.loadProps();
//        String action = "GetMetadataContent";
//        long[] authRes = au.AuthorizationUtil.authorized(URLGCS, "dt", mpegfileS, dg_idS, dt_id, auth, action);
//        if (authRes == null || authRes[0] < 0 || (int) authRes[0] != Integer.parseInt(dt_id)) return Response.status(Response.Status.FORBIDDEN).build();   
        try {
            URL url = new URL(URLGCS + "/api/v1/dtEnc?dt_id=" + dt_id + "&dg_id=" + dg_idS + "&mpegfile=" + mpegfileS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return Response.ok(res,MediaType.APPLICATION_JSON).build();
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
    
    @Secured
    @GET
    @Path("/au")
    public Response getAccessUnit(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("dt_id") String dt_id, 
            @QueryParam("au_id") String au_id,
            @QueryParam("resource") String resource) {
        if(URLGCS == null) UrlUtil.loadProps();
        String action;
        switch (resource) {
            case "data":
                action = "GetDataBySimpleFilter";
                break;
            case "protection":
                action = "GetProtectionAccessUnit";
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }
        long[] authRes = au.AuthorizationUtil.authorized(URLGCS, "dt", mpegfileS, dg_idS, dt_id, auth, action);
        if (authRes == null || authRes[0] < 0 || (int) authRes[0] != Integer.parseInt(dt_id)) return Response.status(Response.Status.FORBIDDEN).build();
            
        try {
            URL url = new URL(URLGCS + "/api/v1/au?au_id="+au_id+"&dt_id=" + dt_id + "&dg_id=" + dg_idS + "&mpegfile=" + mpegfileS + "&resource=" + resource);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return Response.ok(res,MediaType.APPLICATION_JSON).build();
            
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
    
    @Secured
    @GET
    @Path("/auEnc")
    public Response getEncAccessUnit(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("dt_id") String dt_id,
            @QueryParam("au_id") String au_id) {
        if(URLGCS == null) UrlUtil.loadProps();
//        String action = "GetDataBySimpleFilter";
//        long[] authRes = au.AuthorizationUtil.authorized(URLGCS, "dt", mpegfileS, dg_idS, dt_id, jwtUtil.getToken(auth), action);
//        if (authRes == null || authRes[0] < 0 || (int) authRes[0] != Integer.parseInt(dt_id)) return Response.status(Response.Status.FORBIDDEN).build();   
        try {
            URL url = new URL(URLGCS + "/api/v1/auEnc?au_id="+au_id+"&dt_id=" + dt_id + "&dg_id=" + dg_idS + "&mpegfile=" + mpegfileS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return Response.ok(res,MediaType.APPLICATION_JSON).build();
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
    
    @GET
    @Secured
    @Path("/getKey")
    public Response getKeys(@HeaderParam("Authorization") String auth,  
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("dt_id") String dt_idS){
        if(mpegfileS == null || dg_idS == null) return Response.status(Response.Status.BAD_REQUEST).build();
        
        try {
            Properties props = new Properties();
            props.load(WM_GCS.class.getClassLoader().getResourceAsStream("app.properties"));
            URL url = null;
            if(dt_idS != null) url = new URL(props.getProperty("ps.url") + "/api/v1/getKey?dt_id=" + dt_idS + "&dg_id=" + dg_idS + "&mpegfile=" + mpegfileS);
            else url = new URL(props.getProperty("ps.url") + "/api/v1/getKey?dg_id=" + dg_idS + "&mpegfile=" + mpegfileS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            return Response.ok(res,MediaType.APPLICATION_JSON).build();
        } catch (MalformedURLException | ProtocolException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
    @POST
    @Secured
    @Path("/decrypt")
    public Response decrypt(@HeaderParam("Authorization") String auth, 
            @FormDataParam("cipher") InputStream cipher_FIS, 
            @FormDataParam("key") InputStream key_FIS, 
            @FormDataParam("pr") InputStream pr_FIS){
        if(cipher_FIS == null || key_FIS == null || pr_FIS == null) return Response.status(Response.Status.BAD_REQUEST).build();
        StreamDataBodyPart key = new StreamDataBodyPart("key",key_FIS);
        StreamDataBodyPart pr = new StreamDataBodyPart("pr",pr_FIS);
        StreamDataBodyPart cipher = new StreamDataBodyPart("cipher",cipher_FIS);
        
        final FormDataMultiPart multipart;
        String res;
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            
            multipart = (FormDataMultiPart) formDataMultiPart;
            multipart.bodyPart(pr).bodyPart(key).bodyPart(cipher);
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            if(URLGCS == null) UrlUtil.loadProps();
            final WebTarget target = client.target(URLGCS + "/api/v1/decrypt");
            MediaType mediaType = new MediaType(
                multipart.getMediaType().getType(),
                multipart.getMediaType().getSubtype());
            final Response resp = target.request().header("Authorization", auth).post(Entity.entity(multipart,mediaType));
            res = resp.readEntity(String.class);
            multipart.close();
            if(resp.getStatus() == Response.Status.OK.getStatusCode()) return Response.ok(res, MediaType.TEXT_PLAIN).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_GCS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
}

