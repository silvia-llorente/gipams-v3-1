 package wm;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

@ApplicationPath("api") // set the path to REST web services
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        packages("wm").register(MultiPartFeature.class);
    }
}