package rmm.service.keycloak;

import rmm.Models.CustomLog;
import rmm.Security.Secured;
import rmm.Utils.DateUtils;
import rmm.Utils.LogsUtils;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Path("/keycloak/userEvents")
public class KeycloakUserEventLogger {

    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }

    @GET
    @Path("/getDelimitedLogsList")
    @Secured
    public Response getDelimitedLogsList(@QueryParam("max_log") String max_log, @QueryParam("min_log") String min_log, @QueryParam("count") String count) throws IOException {

        if (count.isEmpty()) Response.status(Response.Status.BAD_REQUEST).build();

        int size = Integer.parseInt(count);

        try {
            Date minLogDate = DateUtils.GetDateFromParameter(min_log, 0L);
            Date maxLogDate = DateUtils.GetDateFromParameter(max_log, Long.MAX_VALUE);

            List<CustomLog> logsList = LogsUtils.ReadLogs(maxLogDate, minLogDate, size);

            return Response.ok(logsList, MediaType.APPLICATION_JSON).build();

        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            Response.status(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage()).build();
        }

        return Response.serverError().build();
    }

    @POST
    @Path("/insertUserEvent")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insertUserEvent(String payload) {
        try {
            LogsUtils.WriteLogs(payload);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            Response.status(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage()).build();
        }

        return Response.ok().build();
    }

}
