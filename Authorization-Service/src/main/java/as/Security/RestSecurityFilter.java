package as.Security;

import as.Utils.GoogleUtils;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import com.google.gson.*;
import java.util.Properties;

/**
 *
 * @author alumne
 */
@Provider
//@Secured
@Priority(Priorities.AUTHENTICATION)
public class RestSecurityFilter implements ContainerRequestFilter {
    	 
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            String token = authorizationHeader.substring("Bearer".length()).trim();
            
            String[] chunks = token.split("\\.");
            String dec = new String(Base64.getDecoder().decode(chunks[0]));
            JsonObject json = JsonParser.parseString(dec).getAsJsonObject();
            if (json.get("alg").getAsString().equals("RS256")) {
                String kid = json.get("kid").getAsString(); // get data object
                Properties props = new Properties();
                props.load(RestSecurityFilter.class.getClassLoader().getResourceAsStream("app.properties"));

                JwkProvider provider = new UrlJwkProvider(new URL(props.getProperty("oauth.url")));
                Jwk jwk = provider.get(kid);

                RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
                Algorithm algorithm = Algorithm.RSA256(publicKey, null);
                JWTVerifier verifier = JWT.require(algorithm).build();
                verifier.verify(token);

                return;
            }
  
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println("Access Denied!"+ e);
        } catch (JwkException ex) {
            Logger.getLogger(RestSecurityFilter.class.getName()).log(Level.SEVERE, null, ex);
        }

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