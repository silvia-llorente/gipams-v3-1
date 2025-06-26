package polS;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("api") // set the path to REST web services
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
    }
}