package ss;

import ss.Utils.JWTUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ss.Security.Secured;


/**
 * JAX-RS resource class that provides operations for authentication.
 *
 * @author Suren Konathala
 */
@Path("/v1")
public class SS {
	
    private final JWTUtil jwtUtil = new JWTUtil();
    private static String urlGCS = null;
    public static void setGCSUrl(String url){
        urlGCS = url;
    }
    
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }
    
    @Path("/ownFiles")    
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)    
    public  Response ownMPEGFiles(@HeaderParam("Authorization") String auth) {
        System.out.println(auth);
        String owner = jwtUtil.getUID(auth.substring("Bearer".length()).trim());
        ResultSet rs = db.consults.mpegC.getOwnFiles(owner);
        List< Map<String, String> > response = new ArrayList();
        try {
            while (rs.next()){
                long id = rs.getLong("id");
                Map<String, String> aux =  new HashMap<>();
                aux.put("name", db.consults.mpegC.getString(id, "name"));
                aux.put("id", String.valueOf(id));
                response.add(aux);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SS.class.getName()).log(Level.SEVERE, "Search: ownfiles server error", ex);
            return Response.serverError().build();
        }
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }
    
    @Path("/ownDatasetGroup")    
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response ownDG(@HeaderParam("Authorization") String auth) {
        String owner = jwtUtil.getUID(auth.substring("Bearer".length()).trim());
        ResultSet rs = db.consults.dgC.getOwnDG(owner);
        List< Map<String, String> > response = new ArrayList();
        try {
            while(rs.next()){
                int dg_id = rs.getInt("dg_id");
                Map<String, String> aux =  new HashMap<>();
                aux.put("id", String.valueOf(dg_id));
                response.add(aux);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }
    
    @Path("/ownDataset")    
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response ownDT(@HeaderParam("Authorization") String auth) {
        String owner = jwtUtil.getUID(auth.substring("Bearer".length()).trim());
        ResultSet rs = db.consults.dtC.getOwnDT(owner);
        List< Map<String, String> > response = new ArrayList();
        try {
            while(rs.next()){
                int dt_id = rs.getInt("dt_id");
                Map<String, String> aux =  new HashMap<>();
                aux.put("id", String.valueOf(dt_id));
                response.add(aux);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SS.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }
    
    @Path("/findDatasetGroupMetadata")    
    @Secured
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDGMetadata(@HeaderParam("Authorization") String auth,
                                                               @QueryParam("center") String center,
                                                               @QueryParam("description") String description,
                                                               @QueryParam("title") String title,
                                                               @QueryParam("type") String type)
    {
        try {
            List< Map<String, Object> > response = new ArrayList();
            center = (center == null || center.equals("")) ? null : "%"+center+"%";
            description = (description == null || description.equals("")) ? null : "%"+description+"%";
            title = (title == null || title.equals("")) ? null :"%"+title+"%";
            type = (type == null || type.equals("")) ? null :"%"+type+"%";
            System.out.println("Center" + center + "description: " + description + " title: " + title + "type:" + type);
            ResultSet rs = db.consults.dgC.findByMetadata(center,description,title,type);
            if (rs != null){
                while (rs.next()) {
                    int dg_id = rs.getInt("dg_id");
                    long id = rs.getLong("mpegfile");
                    Map<String, Object> aux =  new HashMap<>();
                    long res[] = au.AuthorizationUtil.authorized(urlGCS, "dg", String.valueOf(id), String.valueOf(dg_id), null, auth, "GetHierarchy");
                    if (res != null && res[0] > 0 && (int) res[0] == dg_id) {
                        res = au.AuthorizationUtil.authorized(urlGCS, "dg", String.valueOf(id), String.valueOf(dg_id), null, auth, "GetMetadataContentDG");
                        if (res != null && res[0] > 0 && (int) res[0] == dg_id) {
                            aux.put("id",id);
                            aux.put("dg_id",dg_id);
                            response.add(aux);
                        }
                    }
                }
            }else{
                Map<String, Object> aux =  new HashMap<>();
                response.add(aux);
            }
            
            return Response.ok(response, MediaType.APPLICATION_JSON).build();
        } catch (SQLException ex) {
            Logger.getLogger(SS.class.getName()).log(Level.SEVERE, "Find dg by metadata ERROR!", ex);
        }
        return Response.serverError().build();
    }
    
    @Path("/findDatasetMetadata") // TODO test-it
    @Secured
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response findDTMetadata(@HeaderParam("Authorization") String auth,
                                                          @QueryParam("center") String center,
                                                          @QueryParam("description") String description,
                                                          @QueryParam("title") String title,
                                                          @QueryParam("type") String type,
                                                          @QueryParam("taxon_id") String taxon_id)
    {
        try {
            center = (center == null || center.equals("")) ? null :"%"+center+"%";
            description = (description == null || description.equals("")) ? null :"%"+description+"%";
            title = (title == null || title.equals("")) ? null :"%"+title+"%";
            type = (type == null || type.equals("")) ? null :"%"+type+"%";
            taxon_id = (taxon_id == null || taxon_id.equals("")) ? null : "%"+taxon_id+"%";
            List< Map<String, String> > response = new ArrayList();
            
            ResultSet rs = db.consults.dtC.findByMetadata(center,description,title,type,taxon_id);
            
            if (rs != null){
                while (rs.next()) {
                    int dt_id = rs.getInt("dt_id");
                    int dg_id = rs.getInt("dg_id");
                    long id = rs.getLong("mpegfile");
                    System.out.println("dt_id: " + dt_id + " dg_id:" + dg_id + " mpegfile:"+ id);
                    Map<String, String> aux =  new HashMap<>();
                    long res[] = au.AuthorizationUtil.authorized(urlGCS, "dt", String.valueOf(id), String.valueOf(dg_id), String.valueOf(dt_id), auth, "GetHierarchy");
                    if (res != null && res[0] > 0 && (int) res[0] == dt_id) {
                        res= au.AuthorizationUtil.authorized(urlGCS, "dt", String.valueOf(id), String.valueOf(dg_id), String.valueOf(dt_id), auth, "GetMetadataContent");
                        if (res != null && res[0] > 0 && (int) res[0] == dt_id) {
                            System.out.println("Hola");
                            aux.put("dt_id",String.valueOf(dt_id));
                            aux.put("dg_id",String.valueOf(dg_id));
                            aux.put("id",String.valueOf(id));
                            response.add(aux);
                        }
                    }
                }
            }else{
                Map<String, String> aux =  new HashMap<>();
                response.add(aux);
            }
            return Response.ok(response, MediaType.APPLICATION_JSON).build();
        } catch (SQLException ex) {
            Logger.getLogger(SS.class.getName()).log(Level.SEVERE, "Find dt by metadata ERROR!", ex);
        }
        return Response.serverError().build();
    }
}

