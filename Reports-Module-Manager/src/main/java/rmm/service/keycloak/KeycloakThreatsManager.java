package rmm.service.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import rmm.Database.DetectionMongoDB;
import rmm.Database.IpBlockingMongoDB;
import rmm.Database.MetricsMongoDB;
import rmm.Database.VariablesMongoDB;
import rmm.Management.Nginx;
import rmm.Models.*;
import rmm.Security.Secured;
import rmm.Utils.DateUtils;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@Path("/keycloak/threatsManager")
public class KeycloakThreatsManager {

    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().entity("Service online").build();
    }

    @GET
    @Path("/getDetectionConfig")
    @Secured
    public Response getDetectionConfig() throws IOException {
        DetectionParamCollection paramsList = DetectionMongoDB.GetInstance().GetAllParams();
        return Response.ok(paramsList, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Path("/updateDetectionConfig")
    @Secured
    public Response updateDetectionConfig(@FormParam("request") String request) throws IOException {

        DetectionParamCollection variables = new DetectionParamCollection();
        ObjectMapper mapper = new ObjectMapper();
        try {
            variables = mapper.readValue(request, DetectionParamCollection.class);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        boolean ok = DetectionMongoDB.GetInstance().UpdateParams(variables);

        if (ok)
            return Response.ok().build();

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Path("/getBlockedIps")
    @Secured
    public Response getBlockedIps() throws IOException {
        IpBlockingCollection ipsList = IpBlockingMongoDB.GetInstance().GetAllBlockedIPs();
        return Response.ok(ipsList, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Path("/unblockIp")
    @Secured
    public Response unblockIp(@FormParam("ip") String ip) throws IOException {
        if (Nginx.GetInstance().UnblockIpAtProxy(ip)) {
            IpBlocking blockingInfo = IpBlockingMongoDB.GetInstance().GetBlockingByIP(ip);
            LogMeasurement measurement = MetricsMongoDB.GetInstance().GetMeasurementByIP(ip);

            double remainingTimeSec = blockingInfo.expirationTime - ((DateUtils.GetNowTime().getTime() - blockingInfo.blockingDate) * 0.001);
            long oldDate = blockingInfo.blockingDate;
            double oldExpiry = blockingInfo.expirationTime;

            blockingInfo.blocked = false;
            blockingInfo.blockingDate = -1L;
            blockingInfo.expirationTime = 0;
            if (!IpBlockingMongoDB.GetInstance().UpdateBlocking(blockingInfo)) {
                Nginx.GetInstance().BlockIpAtProxy(ip, remainingTimeSec);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            measurement.clear();
            if (!MetricsMongoDB.GetInstance().UpdateMeasurement(measurement)) {
                Nginx.GetInstance().BlockIpAtProxy(ip, remainingTimeSec);

                blockingInfo.blocked = true;
                blockingInfo.blockingDate = oldDate;
                blockingInfo.expirationTime = oldExpiry;
                IpBlockingMongoDB.GetInstance().UpdateBlocking(blockingInfo);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            return Response.ok().build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/getExpirationTime")
    @Secured
    public Response getExpirationTime() throws IOException {
        double ExpirationTime = VariablesMongoDB.GetInstance().GetVariableByName("IpBlockingExpirationTime", Double.class);
        return Response.ok(new Variable("IpBlockingExpirationTime", Double.toString(ExpirationTime)), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Path("/updateExpirationTime")
    @Secured
    public Response updateExpirationTime(@FormParam("request") String request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Variable var = mapper.readValue(request, Variable.class);

            boolean ok = VariablesMongoDB.GetInstance().UpdateVariableByName(var.name, Double.parseDouble(var.value.toString()));

            if (ok)
                return Response.ok().build();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }


}
