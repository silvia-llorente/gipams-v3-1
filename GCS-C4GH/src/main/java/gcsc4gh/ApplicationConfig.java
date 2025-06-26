package gcsc4gh;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("api") // set the path to REST web services
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        packages("gcsc4gh").register(MultiPartFeature.class);
    }
}