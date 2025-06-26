package polS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import polS.Security.Secured;

@Path("/v1")
public class PolS {
    
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }
    
    @GET
    @Secured
    @Path("/forgePolicy")
    public Response forgePolicy(@QueryParam("role") String role, @QueryParam("action") String action, @QueryParam("date") String date){
        if(role == null || action == null || date == null
                || role.isEmpty() || action.isEmpty() || date.isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).build();
        String policy_template = new BufferedReader(
            new InputStreamReader(PolS.class.getClassLoader().getResourceAsStream("policy_template.xml"), StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining("\n"));
        String res = String.format(policy_template, role, action, date);
        return Response.ok(res, MediaType.TEXT_PLAIN).build();
    }
    
    
    
}