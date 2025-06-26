package am;

import am.Security.Secured;
import am.db.DB;
import org.json.JSONException;
import org.json.JSONObject;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("/v1")
public class AM {

    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }

    @POST
    @Secured
    @Path("/insert_google_user")
    public Response insert_google_user(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            String responseStr = readResponse(request.getInputStream());

            JSONObject jObj = new JSONObject(responseStr);
            String googleId = jObj.getString("id");
            Logger.getLogger(AM.class.getName()).log(Level.INFO, "BODY in AM: " + googleId);

            boolean insert = false;
            if (DB.GetUserGoogleByGoogleId(googleId) == -1) {
                Logger.getLogger(AM.class.getName()).log(Level.INFO, "TO INSERT id: " + googleId);
                insert = DB.InsertUserGoogle(googleId);
            } else {
                insert = true;
                Logger.getLogger(AM.class.getName()).log(Level.INFO, "id " + googleId + " exists in DB");
            }

            if (!insert) {
                Logger.getLogger(AM.class.getName()).log(Level.SEVERE, "ERROR INSERTING IN DB");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

        } catch (Exception ex) {
            Logger.getLogger(AM.class.getName()).log(Level.SEVERE, "ERROR AM: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Logger.getLogger(AM.class.getName()).log(Level.SEVERE, "OK AM");
        return Response.status(Response.Status.OK).build();
    }

    private String readResponse(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

}
