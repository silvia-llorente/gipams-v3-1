package am;

import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("api") // set the path to REST web services
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
    }
}