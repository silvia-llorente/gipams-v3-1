package am.Security;

import Utils.GoogleUtils;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author alumne
 */
@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class RestSecurityFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Google
        try {
            String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            String tok = authorizationHeader.substring("Bearer".length()).trim();

            Properties props = new Properties();
            props.load(RestSecurityFilter.class.getClassLoader().getResourceAsStream("app.properties"));
            Object token = GoogleUtils.VerifyToken(props, tok);
            if (token != null) {
                return;
            } else {
                Logger.getLogger(RestSecurityFilter.class.getName()).log(Level.SEVERE, "Access denied!");
            }
        } catch (GeneralSecurityException gex) {
            Logger.getLogger(RestSecurityFilter.class.getName()).log(Level.SEVERE, "Access denied!", gex);
        } catch (IOException ioex) {
            Logger.getLogger(RestSecurityFilter.class.getName()).log(Level.SEVERE, "IO Exception", ioex);
        }

        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());

    }
}