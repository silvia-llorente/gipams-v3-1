package wm;

import com.google.gson.JsonArray;
import java.io.BufferedReader;
import wm.Security.Secured;
import wm.Utils.JWTUtil;
import java.io.IOException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import wm.Utils.UrlUtil;


/**
 * JAX-RS resource class that provides operations for authentication.
 *
 * @author Suren Konathala
 */
@Path("/v1")
public class WM_SS {
    private final JWTUtil jwtUtil = new JWTUtil();

    private static String URLSEARCH = null;
    public static void setUrlSearch(String url){
        URLSEARCH = url;
    }
	
    @GET
    @Path("/ping2")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }
    
    @POST
    @Secured
    @Path("/findDatasetGroupMetadata")
    public Response findDatasetGroupMetadata(@HeaderParam("Authorization") String auth, 
            @FormParam("center") String center, 
            @FormParam("description") String description, 
            @FormParam("title") String title, 
            @FormParam("type") String type) {
        if(URLSEARCH == null) UrlUtil.loadProps();        
        try {
            String query = "?";
            boolean first = true;
            String op;
            if (center != null && !center.equals("")) {
                op = (first) ? "" : "&";
                query = query.concat(op+"title="+center);
                first = false;
            }
            if (description != null && !description.equals("")) {
                op = (first) ? "" : "&";
                query = query.concat(op+"description="+description);
                first = false;
            }
            if (title != null && !title.equals("")) {
                op = (first) ? "" : "&";
                query = query.concat(op+"keywords="+title);
                first = false;
            }if (type != null && !type.equals("")) {
                op = (first) ? "" : "&";
                query = query.concat(op+"keywords="+type);
            }
            System.out.println("Query que es fa WM_SS.java(findDatasetGroupMetadata): " + query);
            URL url = new URL(URLSEARCH + "/api/v1/findDatasetGroupMetadata"+query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");

            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            JsonArray arr = new JsonArray();
            arr.add(res);
            System.out.println(arr);
            return Response.ok(res,MediaType.APPLICATION_JSON).build();
        } catch (IOException ex) {
            Logger.getLogger(WM_SS.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }

    @POST
    @Secured
    @Path("/findDatasetMetadata")//TODO: Amb query param!!
    public String findDatasetMetadata(@HeaderParam("Authorization") String auth, 
            @FormParam("title") String title, 
            @FormParam("taxon_id") String taxon_id) {
        if(URLSEARCH == null) UrlUtil.loadProps();
        Map<String, String> body = new HashMap();
        body.put("title", title);
        body.put("taxon_id", taxon_id);
        
        try {
            URL url = new URL(URLSEARCH + "/api/v1/findDatasetMetadata");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            
            try(OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes());			
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            //System.out.println(res);
            return res;
        } catch (IOException ex) {
            Logger.getLogger(WM_SS.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @GET
    @Secured
    @Path("/ownFiles")
    public Response ownFiles(@HeaderParam("Authorization") String auth) {
        try {
            if(URLSEARCH == null) UrlUtil.loadProps();
            URL url = new URL(URLSEARCH + "/api/v1/ownFiles");//your url i.e fetch data from .
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String data;
            data = br.readLine();
            conn.disconnect();
            System.out.println(data);
            return Response.ok(data, MediaType.APPLICATION_JSON).build();
        } catch (ProtocolException ex) {
            Logger.getLogger(WM_SS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_SS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }

    @POST
    @Secured
    @Path("/ownDatasetGroup")
    public Response ownDatasetGroup(@HeaderParam("Authorization") String auth) {
        if(URLSEARCH == null) UrlUtil.loadProps();
        try {
            URL url = new URL(URLSEARCH + "/api/v1/ownDatasetGroup/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String data;
            data = br.readLine();
            conn.disconnect();
            System.out.println(data);
            return Response.ok(data, MediaType.APPLICATION_JSON).build();
        } catch (ProtocolException ex) {
            Logger.getLogger(WM_SS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_SS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }

    @POST
    @Secured
    @Path("/ownDataset")
    public Response ownDataset(@HeaderParam("Authorization") String auth) {
        if(URLSEARCH == null) UrlUtil.loadProps();
        try {
            URL url = new URL(URLSEARCH + "/api/v1/ownDataset/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization" , auth);
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String data;
            data = br.readLine();
            conn.disconnect();
            System.out.println(data);
            return Response.ok(data, MediaType.APPLICATION_JSON).build();
        } catch (ProtocolException ex) {
            Logger.getLogger(WM_SS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WM_SS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.serverError().build();
    }
    
}

