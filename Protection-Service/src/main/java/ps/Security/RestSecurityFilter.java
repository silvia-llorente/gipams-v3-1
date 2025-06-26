package ps.Security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import java.io.IOException;
import java.net.URL;
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
import ps.Utils.UrlUtil;

/**
 *
 * @author alumne
 */
@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class RestSecurityFilter implements ContainerRequestFilter {
    
    private static String oauthUrl = null;
    
    public static void setProp(String aux){
        RestSecurityFilter.oauthUrl = aux;
    }
	 
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            String token = authorizationHeader.substring("Bearer".length()).trim();
            
            String[] chunks = token.split("\\.");
            String dec = new String(Base64.getDecoder().decode(chunks[0]));
            JsonObject json = JsonParser.parseString(dec).getAsJsonObject();
            if(!json.get("alg").getAsString().equals("RS256"))requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            String kid = json.get("kid").getAsString(); // get data object
            if(oauthUrl == null) UrlUtil.loadProps();
            JwkProvider provider = new UrlJwkProvider(new URL(oauthUrl));
            Jwk jwk = provider.get(kid);

            RSAPublicKey publicKey =  (RSAPublicKey)jwk.getPublicKey();
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
 
            //Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
 
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println("Access Denied!"+ e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        } catch (JwkException ex) {
            Logger.getLogger(RestSecurityFilter.class.getName()).log(Level.SEVERE, null, ex);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}