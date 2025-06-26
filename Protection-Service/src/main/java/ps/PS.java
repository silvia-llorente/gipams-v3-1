package ps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ps.DB.Keys;
import ps.PKI.pki;
import ps.Security.Secured;

@Path("/v1")
public class PS {
    
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }
    
    @GET
    @Secured
    @Path("/dg/getRSAKey")
    public Response DGgetRSAKey(@QueryParam("mpegfile") long mpegfile, @QueryParam("dg_id") int dg_id, 
            @QueryParam("keyName") String name, @QueryParam("type") String type){
        String key = Keys.DGgetRSAKey(mpegfile, dg_id, name, type);
        return Response.ok(key, MediaType.TEXT_PLAIN).build();
    }
    @GET
    @Secured
    @Path("/dt/getRSAKey")
    public Response DTgetRSAKey(@QueryParam("mpegfile") long mpegfile, @QueryParam("dg_id") int dg_id, 
            @QueryParam("dt_id") int dt_id, @QueryParam("keyName") String name, @QueryParam("type") String type){
        String key = Keys.DTgetRSAKey(mpegfile, dg_id, dt_id, name, type);
        return Response.ok(key, MediaType.TEXT_PLAIN).build();
    }
    
    
    @GET
    @Secured
    @Path("/dg/genRSAKey")
    public Response DGgenRSAKey(@QueryParam("mpegfile") long mpegfile, @QueryParam("dg_id") int dg_id, 
            @QueryParam("keyName") String name, @QueryParam("type") String type){
        Pair<String, String> p = pki.genRSAKeys();
        String privKey = p.getKey();
        String pubKey = p.getValue();
        if(Keys.DGsetRSAKey(mpegfile, dg_id, name, privKey, pubKey) > 0) {
            if(type.equals("priv")) return Response.ok(privKey, MediaType.TEXT_PLAIN).build();
            return Response.ok(pubKey, MediaType.TEXT_PLAIN).build();
        }
        return Response.ok("-1", MediaType.TEXT_PLAIN).build();
    }
    @GET
    @Secured
    @Path("/dt/genRSAKey")
    public Response DTgenRSAKey(@QueryParam("mpegfile") long mpegfile, @QueryParam("dg_id") int dg_id, 
            @QueryParam("dt_id") int dt_id, @QueryParam("keyName") String name, @QueryParam("type") String type){
        Pair<String, String> p = pki.genRSAKeys();
        String privKey = p.getKey();
        String pubKey = p.getValue();
        if(Keys.DTsetRSAKey(mpegfile, dg_id, dt_id, name, privKey, pubKey) > 0) {
            if(type.equals("priv")) return Response.ok(privKey, MediaType.TEXT_PLAIN).build();
            return Response.ok(pubKey, MediaType.TEXT_PLAIN).build();
        }
        return Response.ok("-1", MediaType.TEXT_PLAIN).build();
    }
    
    
    @GET
    @Secured
    @Path("/dg/getSymKey")
    public Response DGgetSymKey(@QueryParam("mpegfile") long mpegfile, @QueryParam("dg_id") int dg_id, 
            @QueryParam("keyName") String name){
        String key = Keys.DGgetSymKey(mpegfile, dg_id, name);
        return Response.ok(key, MediaType.TEXT_PLAIN).build();
    }
    @GET
    @Secured
    @Path("/dt/getSymKey")
    public Response DTgetSymKey(@QueryParam("mpegfile") long mpegfile, @QueryParam("dg_id") int dg_id, 
            @QueryParam("dt_id") int dt_id, @QueryParam("keyName") String name){
        String key = Keys.DTgetSymKey(mpegfile, dg_id, dt_id, name);
        return Response.ok(key, MediaType.TEXT_PLAIN).build();
    }
    
    
    @GET
    @Secured
    @Path("/dg/genSymKey")
    public Response genDGSymKey(@QueryParam("mpegfile") long mpegfile, @QueryParam("dg_id") int dg_id, 
            @QueryParam("keyName") String name, @QueryParam("size") int size, @QueryParam("noStore") boolean noStore){
        String key = null;
        key = pki.genSymKey("AES", size);
        if(noStore) return Response.ok(key, MediaType.TEXT_PLAIN).build();
        if(Keys.DGsetSymKey(mpegfile, dg_id, name, key) > 0) return Response.ok(key, MediaType.TEXT_PLAIN).build();
        return Response.ok(null, MediaType.TEXT_PLAIN).build();
    }
    @GET
    @Secured
    @Path("/dt/genSymKey")
    public Response genDTSymKey(@QueryParam("mpegfile") long mpegfile, @QueryParam("dg_id") int dg_id,
            @QueryParam("dt_id") int dt_id, @QueryParam("keyName") String name, @QueryParam("size") int size,
            @QueryParam("noStore") boolean noStore){
        String key = null;
        key = pki.genSymKey("AES", size);
        if(noStore) return Response.ok(key, MediaType.TEXT_PLAIN).build();
        if(Keys.DTsetSymKey(mpegfile, dg_id, dt_id, name, key) > 0) return Response.ok(key, MediaType.TEXT_PLAIN).build();
        return Response.ok(null, MediaType.TEXT_PLAIN).build();
    }
    
    
    @POST
    @Secured
    @Path("/addSymKey")
    public Response addSymKey(@QueryParam("mpegfile") Long mpegfile, @QueryParam("dg_id") Integer dg_id, 
            @QueryParam("dt_id") Integer dt_id, @QueryParam("name") String name, @QueryParam("key") String key){
        if(mpegfile != null && dg_id != null && name != null && key != null){
            if(dt_id != null) Keys.DTsetSymKey(mpegfile, dg_id, dt_id, name, key);
            else Keys.DGsetSymKey(mpegfile, dg_id, name, key);
            return Response.ok(0, MediaType.TEXT_PLAIN).build();
        }
        return Response.ok(-1, MediaType.TEXT_PLAIN).build();
    }
    @POST
    @Secured
    @Path("/addRSAKey")
    public Response addRSAKey(@QueryParam("mpegfile") Long mpegfile, @QueryParam("dg_id") Integer dg_id, 
            @QueryParam("dt_id") Integer dt_id, @QueryParam("name") String name, @QueryParam("privKey") String privKey, 
            @QueryParam("pubKey") String pubKey){
        if(mpegfile != null && dg_id != null && name != null && privKey != null && pubKey != null){
            if(dt_id != null) Keys.DTsetRSAKey(mpegfile, dg_id, dt_id, name, privKey, pubKey);
            else Keys.DGsetRSAKey(mpegfile, dg_id, name, privKey, pubKey);
            return Response.ok(0, MediaType.TEXT_PLAIN).build();
        }
        return Response.ok(-1, MediaType.TEXT_PLAIN).build();
    }
    
    
    @DELETE
    @Secured
    @Path("/deleteAllKeys")
    public Response deleteAllKeys(@QueryParam("mpegfile") Long mpegfile, @QueryParam("dg_id") int dg_id, @QueryParam("dt_id") Integer dt_id){
        int res = -1;
        if(dt_id != null){
            res = Keys.DTdeleteAllKeys(mpegfile, dg_id, dt_id);
        } else {
            res = Keys.DGdeleteAllKeys(mpegfile, dg_id);
        }
        return Response.ok(res, MediaType.TEXT_PLAIN).build();
    }
    
    @DELETE
    @Secured
    @Path("/deleteKey")
    public Response deleteKey(@QueryParam("mpegfile") Long mpegfile, @QueryParam("dg_id") int dg_id, @QueryParam("dt_id") Integer dt_id, @QueryParam("keyName") String keyName){
        int res = -1;
        if(dt_id != null){
            res = Keys.DTdeleteKey(mpegfile, dg_id, dt_id, keyName);
        } else {
            res = Keys.DGdeleteKey(mpegfile, dg_id, keyName);
        }
        return Response.ok(res, MediaType.TEXT_PLAIN).build();
    }
    
    
    @GET
    @Secured
    @Path("/getKey")
    public Response getKey(@QueryParam("mpegfile") Long mpegfile, @QueryParam("dg_id") int dg_id, @QueryParam("dt_id") Integer dt_id){
        String key = null;
        ResultSet rs = null;
        if(dt_id != null) rs = Keys.getDTKeys(mpegfile, dg_id, dt_id);
        else rs = Keys.getDGKeys(mpegfile, dg_id);
        try {
            if(rs != null && rs.next()){
                key = rs.getString("symKey");
                if(key == null || key.isEmpty()){
                    key = rs.getString("privKey");
                }
                return Response.ok(key, MediaType.TEXT_PLAIN).build();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.ok(key, MediaType.TEXT_PLAIN).build();
    }
}